package br.edu.unijorge.urnacidada.model;

import java.util.Objects;

/** Voto nulo: o número digitado não corresponde a nenhum candidato ou partido. */
public class VotoNulo extends Voto {

    private final String numeroDigitado;

    public VotoNulo(Cargo cargo, String numeroDigitado) {
        super(cargo);
        this.numeroDigitado = Objects.requireNonNull(numeroDigitado);
    }

    @Override
    public TipoVoto getTipo() { return TipoVoto.NULO; }

    @Override
    public String getNumero() { return numeroDigitado; }

    @Override
    public String getDescricao() { return "Nulo"; }
}
