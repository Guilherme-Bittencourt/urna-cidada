package br.edu.unijorge.urnacidada.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Cargo em disputa (ABSTRAÇÃO). Cada tipo de cargo define quantos dígitos o
 * eleitor digita e como o número digitado vira um voto (método {@link #interpretar}),
 * que é sobrescrito nas subclasses (POLIMORFISMO).
 */
public abstract class Cargo {

    private final String nome;
    private final Map<String, Candidato> candidatos = new LinkedHashMap<>();
    private final Map<Integer, Partido> partidos = new LinkedHashMap<>();

    protected Cargo(String nome) {
        this.nome = Objects.requireNonNull(nome);
    }

    public String getNome() { return nome; }

    public abstract int getQuantidadeDigitos();

    /** Tipo do cargo para exibição/relatórios ("Proporcional" ou "Majoritário"). */
    public abstract String getTipo();

    /**
     * Transforma o número completo digitado em um voto.
     * Só deve ser chamado quando o número tiver {@link #getQuantidadeDigitos()} dígitos.
     */
    public abstract Voto interpretar(String digitos);

    /** Cadastra um candidato. Subclasses validam o tipo de candidato aceito. */
    public void adicionarCandidato(Candidato candidato) {
        if (candidato.getQuantidadeDigitos() != getQuantidadeDigitos()) {
            throw new IllegalArgumentException(
                    "Candidato " + candidato + " não é compatível com o cargo " + nome);
        }
        if (candidatos.containsKey(candidato.getNumero())) {
            throw new IllegalArgumentException("Número já cadastrado: " + candidato.getNumero());
        }
        candidatos.put(candidato.getNumero(), candidato);
        partidos.putIfAbsent(candidato.getPartido().getNumero(), candidato.getPartido());
    }

    public Optional<Candidato> buscarCandidato(String numero) {
        return Optional.ofNullable(candidatos.get(numero));
    }

    /** Procura o partido pelos 2 primeiros dígitos do número. */
    public Optional<Partido> buscarPartido(String digitos) {
        if (digitos == null || digitos.length() < 2) {
            return Optional.empty();
        }
        int numeroPartido = Integer.parseInt(digitos.substring(0, 2));
        return Optional.ofNullable(partidos.get(numeroPartido));
    }

    public Collection<Candidato> getCandidatos() {
        return Collections.unmodifiableCollection(candidatos.values());
    }

    public Collection<Partido> getPartidos() {
        return Collections.unmodifiableCollection(partidos.values());
    }

    protected void validarDigitos(String digitos) {
        if (digitos == null || digitos.length() != getQuantidadeDigitos() || !digitos.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(
                    "O cargo " + nome + " exige " + getQuantidadeDigitos() + " dígitos. Recebido: " + digitos);
        }
    }

    @Override
    public String toString() {
        return nome;
    }
}
