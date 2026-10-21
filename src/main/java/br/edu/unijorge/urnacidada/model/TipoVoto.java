package br.edu.unijorge.urnacidada.model;

/** Tipos de voto possíveis na urna. */
public enum TipoVoto {
    NOMINAL("Voto nominal", true),
    LEGENDA("Voto de legenda", true),
    BRANCO("Voto em branco", false),
    NULO("Voto nulo", false);

    private final String descricao;
    private final boolean valido;

    TipoVoto(String descricao, boolean valido) {
        this.descricao = descricao;
        this.valido = valido;
    }

    public String getDescricao() { return descricao; }

    /** Votos válidos = nominais + legenda (brancos e nulos não entram no cálculo). */
    public boolean isValido() { return valido; }
}
