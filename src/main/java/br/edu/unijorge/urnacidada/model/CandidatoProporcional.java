package br.edu.unijorge.urnacidada.model;

/**
 * Candidato a cargo proporcional (ex.: vereador, deputado).
 * Número com 5 dígitos: os 2 primeiros identificam o partido.
 */
public class CandidatoProporcional extends Candidato {

    public static final int DIGITOS = 5;

    public CandidatoProporcional(String numero, String nome, Partido partido) {
        super(numero, nome, partido);
        if (numero.length() != DIGITOS) {
            throw new IllegalArgumentException("Candidato proporcional precisa de " + DIGITOS + " dígitos");
        }
    }

    @Override
    public int getQuantidadeDigitos() {
        return DIGITOS;
    }
}
