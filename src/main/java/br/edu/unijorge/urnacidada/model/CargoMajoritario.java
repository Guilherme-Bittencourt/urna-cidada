package br.edu.unijorge.urnacidada.model;

/**
 * Cargo majoritário (prefeito): 2 dígitos.
 * Candidato existe -> nominal. Senão -> nulo (não existe voto de legenda).
 */
public class CargoMajoritario extends Cargo {

    public CargoMajoritario(String nome) {
        super(nome);
    }

    @Override
    public int getQuantidadeDigitos() {
        return 2;
    }

    @Override
    public Voto interpretar(String numero) {
        Candidato candidato = buscarCandidato(numero);
        if (candidato != null) {
            return new Voto(getNome(), TipoVoto.NOMINAL, numero, candidato.getNome());
        }
        return new Voto(getNome(), TipoVoto.NULO, numero, "Nulo");
    }
}
