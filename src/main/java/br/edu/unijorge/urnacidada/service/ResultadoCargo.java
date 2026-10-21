package br.edu.unijorge.urnacidada.service;

import java.util.List;

/**
 * Resultado da apuração de um cargo.
 *
 * @param cargo       nome do cargo
 * @param tipoCargo   "Proporcional" ou "Majoritário"
 * @param candidatos  votos nominais por candidato (ordenado do mais votado ao menos votado)
 * @param partidos    votos por partido = nominais + legenda (só faz sentido no proporcional)
 * @param legenda     total de votos de legenda
 * @param brancos     total de votos em branco
 * @param nulos       total de votos nulos
 */
public record ResultadoCargo(String cargo, String tipoCargo, List<Linha> candidatos, List<Linha> partidos,
                             long legenda, long brancos, long nulos) {

    /** Uma linha do boletim: número, nome, partido e quantidade de votos. */
    public record Linha(String numero, String nome, String siglaPartido, long votos) {
    }

    /** Votos válidos = nominais + legenda. */
    public long validos() {
        return candidatos.stream().mapToLong(Linha::votos).sum() + legenda;
    }

    /** Comparecimento para este cargo (todos os votos). */
    public long total() {
        return validos() + brancos + nulos;
    }

    /** Percentual sobre os votos válidos (regra usada pela Justiça Eleitoral nos cargos majoritários). */
    public double percentualValidos(long votos) {
        long v = validos();
        return v == 0 ? 0.0 : 100.0 * votos / v;
    }

    public boolean isProporcional() {
        return "Proporcional".equals(tipoCargo);
    }
}
