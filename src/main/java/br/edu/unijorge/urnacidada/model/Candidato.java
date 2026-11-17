package br.edu.unijorge.urnacidada.model;

/**
 * Classe abstrata com o que todo candidato tem (ABSTRAÇÃO).
 * Atributos privados, acessados só por getters (ENCAPSULAMENTO).
 */
public abstract class Candidato {

    private final String numero;
    private final String nome;
    private final Partido partido;

    public Candidato(String numero, String nome, Partido partido) {
        this.numero = numero;
        this.nome = nome;
        this.partido = partido;
    }

    public String getNumero() { return numero; }
    public String getNome() { return nome; }
    public Partido getPartido() { return partido; }

    /** Linha extra mostrada na tela da urna. Cada tipo de candidato responde diferente (POLIMORFISMO). */
    public abstract String getInfoExtra();
}
