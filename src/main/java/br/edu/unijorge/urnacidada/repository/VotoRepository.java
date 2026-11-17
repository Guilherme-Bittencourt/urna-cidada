package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.Voto;

/**
 * Onde os votos são gravados (ABSTRAÇÃO via interface).
 * A urna só conhece esta interface; quem grava de verdade é o FirestoreVotoRepository.
 */
public interface VotoRepository {

    void salvar(Voto voto);
}
