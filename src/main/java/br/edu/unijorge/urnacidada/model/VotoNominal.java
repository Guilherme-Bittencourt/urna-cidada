package br.edu.unijorge.urnacidada.model;

import java.util.Objects;

/** Voto em um candidato que existe. */
public class VotoNominal extends Voto {

    private final Candidato candidato;

    public VotoNominal(Cargo cargo, Candidato candidato) {
        super(cargo);
        this.candidato = Objects.requireNonNull(candidato);
    }

    public Candidato getCandidato() { return candidato; }

    @Override
    public TipoVoto getTipo() { return TipoVoto.NOMINAL; }

    @Override
    public String getNumero() { return candidato.getNumero(); }

    @Override
    public String getDescricao() { return candidato.getNome(); }

    @Override
    public String getSiglaPartido() { return candidato.getPartido().getSigla(); }
}
