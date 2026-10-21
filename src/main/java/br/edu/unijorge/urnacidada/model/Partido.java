package br.edu.unijorge.urnacidada.model;

import java.awt.Color;
import java.util.Objects;

/**
 * Partido político FICTÍCIO usado na simulação.
 * O número do partido tem sempre 2 dígitos e é o prefixo do número dos seus candidatos.
 */
public class Partido {

    private final int numero;
    private final String sigla;
    private final String nome;
    private final Color cor;

    public Partido(int numero, String sigla, String nome, Color cor) {
        if (numero < 10 || numero > 99) {
            throw new IllegalArgumentException("O número do partido deve ter 2 dígitos: " + numero);
        }
        this.numero = numero;
        this.sigla = Objects.requireNonNull(sigla);
        this.nome = Objects.requireNonNull(nome);
        this.cor = Objects.requireNonNull(cor);
    }

    public int getNumero() { return numero; }
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public Color getCor() { return cor; }

    @Override
    public String toString() {
        return numero + " - " + sigla + " (" + nome + ")";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Partido p && p.numero == numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }
}
