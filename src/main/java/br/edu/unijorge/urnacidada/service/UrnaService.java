package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.Eleicao;
import br.edu.unijorge.urnacidada.model.Partido;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.model.VotoBranco;
import br.edu.unijorge.urnacidada.repository.VotoRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Regras de funcionamento da urna (máquina de estados), independentes da tela.
 *
 * Fluxo de um eleitor: para cada cargo da eleição, em ordem,
 * digita o número (ou aperta BRANCO), pode CORRIGIR, e CONFIRMA.
 * Depois do último cargo a urna mostra FIM e aguarda o próximo eleitor.
 */
public class UrnaService {

    public enum Estado {
        /** Aguardando/recebendo os dígitos do número. */
        DIGITANDO,
        /** O eleitor apertou BRANCO e falta confirmar. */
        BRANCO,
        /** Todos os cargos foram votados. */
        FIM
    }

    private final Eleicao eleicao;
    private final VotoRepository repositorio;
    private final Executor executorGravacao;
    private final List<UrnaListener> listeners = new CopyOnWriteArrayList<>();

    private final StringBuilder digitos = new StringBuilder();
    private int indiceCargo;
    private Estado estado = Estado.DIGITANDO;
    private int eleitoresQueVotaram;

    /** Construtor usado pelo programa: grava os votos em segundo plano (não trava a tela). */
    public UrnaService(Eleicao eleicao, VotoRepository repositorio) {
        this(eleicao, repositorio, criarExecutorPadrao());
    }

    /** Construtor que permite escolher como a gravação roda (os testes usam execução imediata). */
    public UrnaService(Eleicao eleicao, VotoRepository repositorio, Executor executorGravacao) {
        this.eleicao = Objects.requireNonNull(eleicao);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.executorGravacao = Objects.requireNonNull(executorGravacao);
        if (eleicao.getCargos().isEmpty()) {
            throw new IllegalArgumentException("A eleição precisa de pelo menos um cargo");
        }
    }

    private static ExecutorService criarExecutorPadrao() {
        // uma única thread: os votos são gravados na ordem em que foram confirmados
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "gravacao-votos");
            t.setDaemon(true);
            return t;
        });
    }

    // ------------------------------------------------------------------ consulta de estado

    public Eleicao getEleicao() { return eleicao; }
    public Estado getEstado() { return estado; }
    public String getDigitos() { return digitos.toString(); }
    public int getEleitoresQueVotaram() { return eleitoresQueVotaram; }
    public VotoRepository getRepositorio() { return repositorio; }

    /** Cargo sendo votado agora, ou vazio quando a urna está em FIM. */
    public Optional<Cargo> getCargoAtual() {
        return estado == Estado.FIM ? Optional.empty() : Optional.of(eleicao.getCargos().get(indiceCargo));
    }

    public boolean isNumeroCompleto() {
        return getCargoAtual().map(c -> digitos.length() == c.getQuantidadeDigitos()).orElse(false);
    }

    /** Partido identificado pelos 2 primeiros dígitos (mostrado enquanto o eleitor digita). */
    public Optional<Partido> getPartidoDigitado() {
        return getCargoAtual().flatMap(c -> c.buscarPartido(digitos.toString()));
    }

    /** O voto que será registrado se o eleitor apertar CONFIRMA agora (vazio se ainda não pode confirmar). */
    public Optional<Voto> getVotoPrevisto() {
        Optional<Cargo> cargo = getCargoAtual();
        if (cargo.isEmpty()) {
            return Optional.empty();
        }
        if (estado == Estado.BRANCO) {
            return Optional.of(new VotoBranco(cargo.get()));
        }
        if (isNumeroCompleto()) {
            return Optional.of(cargo.get().interpretar(digitos.toString()));
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ teclas da urna

    /** Tecla numérica. Retorna {@code false} se a tecla foi ignorada. */
    public boolean digitar(int numero) {
        if (numero < 0 || numero > 9) {
            throw new IllegalArgumentException("Tecla inválida: " + numero);
        }
        if (estado != Estado.DIGITANDO || isNumeroCompleto()) {
            return false;
        }
        digitos.append(numero);
        notificarEstado();
        return true;
    }

    /** Tecla CORRIGE: apaga tudo o que foi digitado para o cargo atual. */
    public boolean corrigir() {
        if (estado == Estado.FIM) {
            return false;
        }
        digitos.setLength(0);
        estado = Estado.DIGITANDO;
        notificarEstado();
        return true;
    }

    /**
     * Tecla BRANCO. Igual à urna real: só funciona se nenhum número foi digitado
     * (se já digitou, é preciso apertar CORRIGE antes).
     */
    public boolean votarEmBranco() {
        if (estado != Estado.DIGITANDO || digitos.length() > 0) {
            return false;
        }
        estado = Estado.BRANCO;
        notificarEstado();
        return true;
    }

    /**
     * Tecla CONFIRMA. Registra o voto do cargo atual e passa para o próximo.
     *
     * @return o voto registrado, ou vazio se ainda não era possível confirmar
     */
    public Optional<Voto> confirmar() {
        Optional<Voto> previsto = getVotoPrevisto();
        if (previsto.isEmpty()) {
            return Optional.empty();
        }
        Voto voto = previsto.get();
        listeners.forEach(l -> l.votoConfirmado(voto));
        gravar(voto);
        avancarCargo();
        return Optional.of(voto);
    }

    /** Ação do mesário: libera a urna para o próximo eleitor. */
    public void liberarProximoEleitor() {
        indiceCargo = 0;
        digitos.setLength(0);
        estado = Estado.DIGITANDO;
        notificarEstado();
    }

    // ------------------------------------------------------------------ interno

    private void avancarCargo() {
        indiceCargo++;
        digitos.setLength(0);
        if (indiceCargo >= eleicao.getCargos().size()) {
            estado = Estado.FIM;
            eleitoresQueVotaram++;
        } else {
            estado = Estado.DIGITANDO;
        }
        notificarEstado();
    }

    private void gravar(Voto voto) {
        executorGravacao.execute(() -> {
            try {
                repositorio.salvar(voto.toRegistro());
                listeners.forEach(l -> l.votoGravado(voto));
            } catch (Exception e) {
                listeners.forEach(l -> l.erroAoGravar(voto, e));
            }
        });
    }

    public void adicionarListener(UrnaListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removerListener(UrnaListener listener) {
        listeners.remove(listener);
    }

    private void notificarEstado() {
        listeners.forEach(UrnaListener::estadoAlterado);
    }
}
