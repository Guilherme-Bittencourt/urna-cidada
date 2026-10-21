package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.RegistroVoto;

import java.util.List;

/**
 * Contrato de persistência dos votos (ABSTRAÇÃO via interface).
 * A urna não sabe se está gravando no Firebase ou na memória: basta trocar a implementação.
 */
public interface VotoRepository {

    /** Grava um voto. Pode lançar {@link RepositorioException} em caso de falha. */
    void salvar(RegistroVoto voto);

    /** Lê todos os votos gravados (usado na apuração / boletim de urna). */
    List<RegistroVoto> listarTodos();

    /** Apaga todos os votos (zerar a urna antes de uma nova votação). */
    void apagarTodos();

    /** Texto curto para a barra de status, ex.: "Firebase (Cloud Firestore)". */
    String getDescricao();
}
