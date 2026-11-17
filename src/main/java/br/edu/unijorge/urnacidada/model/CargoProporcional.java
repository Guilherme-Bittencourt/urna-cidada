package br.edu.unijorge.urnacidada.model;

/**
 * Cargo proporcional (vereador): 5 dígitos.
 * Candidato existe -> nominal. Só o partido existe -> legenda. Nenhum dos dois -> nulo.
 */
public class CargoProporcional extends Cargo {

    public CargoProporcional(String nome) {
        super(nome);
    }

    @Override
    public int getQuantidadeDigitos() {
        return 5;
    }

    @Override
    public Voto interpretar(String numero) {
        Candidato candidato = buscarCandidato(numero);
        if (candidato != null) {
            return new Voto(getNome(), TipoVoto.NOMINAL, numero, candidato.getNome());
        }
        Partido partido = buscarPartido(numero);
        if (partido != null) {
            return new Voto(getNome(), TipoVoto.LEGENDA, numero, "Legenda " + partido.getSigla());
        }
        return new Voto(getNome(), TipoVoto.NULO, numero, "Nulo");
    }
}
