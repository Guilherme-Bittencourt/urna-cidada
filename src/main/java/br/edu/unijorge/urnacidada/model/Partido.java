package br.edu.unijorge.urnacidada.model;

/** Partido fictício. O número (2 dígitos) é o começo do número dos seus candidatos. */
public class Partido {

    private final int numero;
    private final String sigla;
    private final String nome;

    public Partido(int numero, String sigla, String nome) {
        this.numero = numero;
        this.sigla = sigla;
        this.nome = nome;
    }

    public int getNumero() { return numero; }
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
}
