package br.edu.unijorge.urnacidada.model;

/** Candidato a vereador: número com 5 dígitos (HERANÇA de Candidato). */
public class CandidatoProporcional extends Candidato {

    public CandidatoProporcional(String numero, String nome, Partido partido) {
        super(numero, nome, partido);
    }

    @Override
    public String getInfoExtra() {
        return "";
    }
}
