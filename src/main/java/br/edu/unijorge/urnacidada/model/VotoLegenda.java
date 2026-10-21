package br.edu.unijorge.urnacidada.model;

import java.util.Objects;

/**
 * Voto de legenda: só existe em cargos proporcionais. Acontece quando o eleitor
 * digita um número cujo partido existe, mas o candidato não (ou digita só o
 * número do partido seguido de zeros). O voto conta para o partido.
 */
public class VotoLegenda extends Voto {

    private final Partido partido;
    private final String numeroDigitado;

    public VotoLegenda(Cargo cargo, Partido partido, String numeroDigitado) {
        super(cargo);
        this.partido = Objects.requireNonNull(partido);
        this.numeroDigitado = Objects.requireNonNull(numeroDigitado);
    }

    public Partido getPartido() { return partido; }

    @Override
    public TipoVoto getTipo() { return TipoVoto.LEGENDA; }

    @Override
    public String getNumero() { return numeroDigitado; }

    @Override
    public String getDescricao() { return "Legenda " + partido.getSigla(); }

    @Override
    public String getSiglaPartido() { return partido.getSigla(); }
}
