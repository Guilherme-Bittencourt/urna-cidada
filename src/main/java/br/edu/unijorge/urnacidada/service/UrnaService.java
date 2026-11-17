package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.repository.VotoRepository;

import java.util.List;

/**
 * Regras da urna, separadas da tela.
 * Para cada cargo (em ordem): digita o número ou aperta BRANCO, pode CORRIGIR, e CONFIRMA.
 * Depois do último cargo a urna fica em FIM até liberar o próximo eleitor.
 */
public class UrnaService {

    private final List<Cargo> cargos;
    private final VotoRepository repositorio;

    private String digitos = "";
    private boolean branco = false;
    private int indiceCargo = 0;

    public UrnaService(List<Cargo> cargos, VotoRepository repositorio) {
        this.cargos = cargos;
        this.repositorio = repositorio;
    }

    public String getDigitos() { return digitos; }
    public boolean isBranco() { return branco; }

    public boolean isFim() {
        return indiceCargo >= cargos.size();
    }

    /** Cargo sendo votado agora (null quando a votação terminou). */
    public Cargo getCargoAtual() {
        return isFim() ? null : cargos.get(indiceCargo);
    }

    public boolean isNumeroCompleto() {
        return !isFim() && digitos.length() == getCargoAtual().getQuantidadeDigitos();
    }

    /** O voto que será gravado se apertar CONFIRMA agora (null se ainda não pode confirmar). */
    public Voto getVotoAtual() {
        if (isFim()) {
            return null;
        }
        if (branco) {
            return new Voto(getCargoAtual().getNome(), TipoVoto.BRANCO, "", "Branco");
        }
        if (isNumeroCompleto()) {
            return getCargoAtual().interpretar(digitos);
        }
        return null;
    }

    public void digitar(int tecla) {
        if (!isFim() && !branco && !isNumeroCompleto()) {
            digitos += tecla;
        }
    }

    public void corrigir() {
        digitos = "";
        branco = false;
    }

    /** Igual à urna real: BRANCO só funciona se nada foi digitado. */
    public void votarEmBranco() {
        if (!isFim() && digitos.isEmpty()) {
            branco = true;
        }
    }

    /** Grava o voto do cargo atual e passa para o próximo. Retorna false se ainda não dava para confirmar. */
    public boolean confirmar() {
        Voto voto = getVotoAtual();
        if (voto == null) {
            return false;
        }
        repositorio.salvar(voto);
        indiceCargo++;
        corrigir();
        return true;
    }

    public void proximoEleitor() {
        indiceCargo = 0;
        corrigir();
    }
}
