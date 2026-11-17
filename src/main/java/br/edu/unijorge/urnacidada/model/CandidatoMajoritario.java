package br.edu.unijorge.urnacidada.model;

/** Candidato a prefeito: número com 2 dígitos e tem um vice (HERANÇA de Candidato). */
public class CandidatoMajoritario extends Candidato {

    private final String vice;

    public CandidatoMajoritario(String numero, String nome, Partido partido, String vice) {
        super(numero, nome, partido);
        this.vice = vice;
    }

    public String getVice() { return vice; }

    @Override
    public String getInfoExtra() {
        return "Vice: " + vice;
    }
}
