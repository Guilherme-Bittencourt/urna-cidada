package br.edu.unijorge.urnacidada.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Voto abstrato (ABSTRAÇÃO). Cada subclasse sabe dizer o próprio tipo,
 * número e descrição, e a urna trata todas de forma igual (POLIMORFISMO).
 *
 * Importante: o voto NÃO guarda nenhuma informação do eleitor (sigilo do voto).
 */
public abstract class Voto {

    private final Cargo cargo;
    private final Instant dataHora;

    protected Voto(Cargo cargo) {
        this.cargo = Objects.requireNonNull(cargo);
        this.dataHora = Instant.now();
    }

    public Cargo getCargo() { return cargo; }
    public Instant getDataHora() { return dataHora; }

    public abstract TipoVoto getTipo();

    /** Número digitado (vazio no voto em branco). */
    public abstract String getNumero();

    /** Texto que identifica o destino do voto na apuração. */
    public abstract String getDescricao();

    /** Sigla do partido beneficiado, ou {@code null} se não houver (branco/nulo). */
    public String getSiglaPartido() {
        return null;
    }

    public boolean isValido() {
        return getTipo().isValido();
    }

    /** Converte para o formato simples que é gravado no banco de dados. */
    public RegistroVoto toRegistro() {
        return new RegistroVoto(cargo.getNome(), getTipo(), getNumero(), getDescricao(), getSiglaPartido());
    }

    @Override
    public String toString() {
        return cargo.getNome() + ": " + getTipo().getDescricao() + " -> " + getDescricao();
    }
}
