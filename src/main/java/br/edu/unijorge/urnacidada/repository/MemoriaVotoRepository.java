package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.RegistroVoto;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda os votos apenas na memória do computador.
 * Usado nos testes e como modo offline quando o Firebase não está configurado.
 */
public class MemoriaVotoRepository implements VotoRepository {

    private final List<RegistroVoto> votos = new ArrayList<>();

    @Override
    public synchronized void salvar(RegistroVoto voto) {
        votos.add(voto);
    }

    @Override
    public synchronized List<RegistroVoto> listarTodos() {
        return new ArrayList<>(votos);
    }

    @Override
    public synchronized void apagarTodos() {
        votos.clear();
    }

    @Override
    public String getDescricao() {
        return "Modo offline (votos só na memória)";
    }
}
