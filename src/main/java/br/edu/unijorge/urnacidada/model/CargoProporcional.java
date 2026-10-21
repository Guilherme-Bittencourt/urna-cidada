package br.edu.unijorge.urnacidada.model;

/**
 * Cargo proporcional (vereador, deputado estadual/federal).
 * Regras: 5 dígitos; se o candidato não existir mas o partido sim, é voto de legenda;
 * se nem o partido existir, é voto nulo.
 */
public class CargoProporcional extends Cargo {

    public CargoProporcional(String nome) {
        super(nome);
    }

    @Override
    public int getQuantidadeDigitos() {
        return CandidatoProporcional.DIGITOS;
    }

    @Override
    public String getTipo() {
        return "Proporcional";
    }

    @Override
    public void adicionarCandidato(Candidato candidato) {
        if (!(candidato instanceof CandidatoProporcional)) {
            throw new IllegalArgumentException("Cargo proporcional só aceita CandidatoProporcional");
        }
        super.adicionarCandidato(candidato);
    }

    @Override
    public Voto interpretar(String digitos) {
        validarDigitos(digitos);
        return buscarCandidato(digitos)
                .<Voto>map(c -> new VotoNominal(this, c))
                .or(() -> buscarPartido(digitos).map(p -> new VotoLegenda(this, p, digitos)))
                .orElseGet(() -> new VotoNulo(this, digitos));
    }
}
