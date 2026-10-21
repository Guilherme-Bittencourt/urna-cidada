package br.edu.unijorge.urnacidada.model;

/**
 * Representação "achatada" de um voto, no formato em que ele é gravado
 * no banco (Firestore ou memória) e lido de volta na apuração.
 *
 * @param cargo        nome do cargo (ex.: "Vereador")
 * @param tipo         NOMINAL, LEGENDA, BRANCO ou NULO
 * @param numero       número digitado ("" para branco)
 * @param descricao    nome do candidato, "Legenda XXX", "Branco" ou "Nulo"
 * @param siglaPartido partido beneficiado (null para branco/nulo)
 */
public record RegistroVoto(String cargo, TipoVoto tipo, String numero, String descricao, String siglaPartido) {
}
