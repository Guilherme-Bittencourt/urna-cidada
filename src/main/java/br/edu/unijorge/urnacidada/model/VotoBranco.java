package br.edu.unijorge.urnacidada.model;

/** Voto em branco: o eleitor apertou BRANCO sem digitar nenhum número. */
public class VotoBranco extends Voto {

    public VotoBranco(Cargo cargo) {
        super(cargo);
    }

    @Override
    public TipoVoto getTipo() { return TipoVoto.BRANCO; }

    @Override
    public String getNumero() { return ""; }

    @Override
    public String getDescricao() { return "Branco"; }
}
