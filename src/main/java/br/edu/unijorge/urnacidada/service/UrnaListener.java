package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.model.Voto;

/**
 * Observador da urna (padrão Observer): a interface gráfica se registra aqui
 * para ser avisada quando algo muda. Todos os métodos têm implementação vazia,
 * então quem implementa sobrescreve só o que precisa.
 */
public interface UrnaListener {

    /** O estado da urna mudou (dígito, correção, branco, troca de cargo...). */
    default void estadoAlterado() { }

    /** Um voto foi confirmado (antes de ser gravado). */
    default void votoConfirmado(Voto voto) { }

    /** O voto foi gravado com sucesso no banco. Pode ser chamado fora da thread do Swing. */
    default void votoGravado(Voto voto) { }

    /** Falha ao gravar o voto. Pode ser chamado fora da thread do Swing. */
    default void erroAoGravar(Voto voto, Exception erro) { }
}
