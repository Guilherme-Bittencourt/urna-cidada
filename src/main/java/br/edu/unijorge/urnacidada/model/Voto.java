package br.edu.unijorge.urnacidada.model;

/** Um voto já interpretado. Não guarda nada sobre o eleitor (voto secreto). */
public class Voto {

    private final String cargo;
    private final TipoVoto tipo;
    private final String numero;
    private final String descricao;

    public Voto(String cargo, TipoVoto tipo, String numero, String descricao) {
        this.cargo = cargo;
        this.tipo = tipo;
        this.numero = numero;
        this.descricao = descricao;
    }

    public String getCargo() { return cargo; }
    public TipoVoto getTipo() { return tipo; }
    public String getNumero() { return numero; }
    public String getDescricao() { return descricao; }
}
