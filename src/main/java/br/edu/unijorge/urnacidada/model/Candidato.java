package br.edu.unijorge.urnacidada.model;

import java.util.Objects;

/**
 * Classe abstrata base de todos os candidatos (ABSTRAÇÃO + HERANÇA).
 * Os atributos são privados e só podem ser lidos por getters (ENCAPSULAMENTO).
 */
public abstract class Candidato {

    private final String numero;
    private final String nome;
    private final Partido partido;

    protected Candidato(String numero, String nome, Partido partido) {
        this.numero = Objects.requireNonNull(numero);
        this.nome = Objects.requireNonNull(nome);
        this.partido = Objects.requireNonNull(partido);
        if (!numero.startsWith(String.valueOf(partido.getNumero()))) {
            throw new IllegalArgumentException(
                    "O número " + numero + " deve começar com o número do partido " + partido.getNumero());
        }
    }

    public String getNumero() { return numero; }
    public String getNome() { return nome; }
    public Partido getPartido() { return partido; }

    /** Quantidade de dígitos esperada para o número deste tipo de candidato. */
    public abstract int getQuantidadeDigitos();

    /** Iniciais usadas no "retrato" desenhado na tela da urna. */
    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[partes.length - 1].substring(0, 1);
        }
        return ini.toUpperCase();
    }

    @Override
    public String toString() {
        return numero + " - " + nome + " (" + partido.getSigla() + ")";
    }
}
