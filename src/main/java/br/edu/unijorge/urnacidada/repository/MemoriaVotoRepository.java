package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.Voto;

import java.util.ArrayList;
import java.util.List;

/**
 * Plano B: guarda os votos só na memória, para a urna funcionar sem o Firebase.
 * Outra implementação da mesma interface (POLIMORFISMO): a urna não percebe a diferença.
 */
public class MemoriaVotoRepository implements VotoRepository {

    private final List<Voto> votos = new ArrayList<>();

    @Override
    public void salvar(Voto voto) {
        votos.add(voto);
        System.out.println("[offline] " + voto.getCargo() + ": " + voto.getDescricao());
    }
}
