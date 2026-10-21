package br.edu.unijorge.urnacidada.model;

import java.util.Objects;

/**
 * Candidato a cargo majoritário (ex.: prefeito, governador, presidente).
 * Número com 2 dígitos (igual ao do partido) e possui um vice na chapa.
 */
public class CandidatoMajoritario extends Candidato {

    public static final int DIGITOS = 2;

    private final String vice;

    public CandidatoMajoritario(String numero, String nome, Partido partido, String vice) {
        super(numero, nome, partido);
        if (numero.length() != DIGITOS) {
            throw new IllegalArgumentException("Candidato majoritário precisa de " + DIGITOS + " dígitos");
        }
        this.vice = Objects.requireNonNull(vice);
    }

    public String getVice() { return vice; }

    @Override
    public int getQuantidadeDigitos() {
        return DIGITOS;
    }
}
