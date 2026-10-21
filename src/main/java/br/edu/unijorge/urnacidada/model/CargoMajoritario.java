package br.edu.unijorge.urnacidada.model;

/**
 * Cargo majoritário (prefeito, governador, presidente).
 * Regras: 2 dígitos; ou o candidato existe (voto nominal) ou o voto é nulo.
 * Não existe voto de legenda em cargo majoritário.
 */
public class CargoMajoritario extends Cargo {

    private final String nomeCargoVice;

    /**
     * @param nome          ex.: "Prefeito"
     * @param nomeCargoVice ex.: "Vice-prefeito" (aparece na tela da urna)
     */
    public CargoMajoritario(String nome, String nomeCargoVice) {
        super(nome);
        this.nomeCargoVice = nomeCargoVice;
    }

    public String getNomeCargoVice() { return nomeCargoVice; }

    @Override
    public int getQuantidadeDigitos() {
        return CandidatoMajoritario.DIGITOS;
    }

    @Override
    public String getTipo() {
        return "Majoritário";
    }

    @Override
    public void adicionarCandidato(Candidato candidato) {
        if (!(candidato instanceof CandidatoMajoritario)) {
            throw new IllegalArgumentException("Cargo majoritário só aceita CandidatoMajoritario");
        }
        super.adicionarCandidato(candidato);
    }

    @Override
    public Voto interpretar(String digitos) {
        validarDigitos(digitos);
        return buscarCandidato(digitos)
                .<Voto>map(c -> new VotoNominal(this, c))
                .orElseGet(() -> new VotoNulo(this, digitos));
    }
}
