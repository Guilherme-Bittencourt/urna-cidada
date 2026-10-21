package br.edu.unijorge.urnacidada.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Uma eleição: nome + lista ordenada de cargos (a urna pede o voto nesta ordem). */
public class Eleicao {

    private final String nome;
    private final List<Cargo> cargos = new ArrayList<>();

    public Eleicao(String nome) {
        this.nome = Objects.requireNonNull(nome);
    }

    public String getNome() { return nome; }

    public Eleicao adicionarCargo(Cargo cargo) {
        cargos.add(Objects.requireNonNull(cargo));
        return this;
    }

    public List<Cargo> getCargos() {
        return Collections.unmodifiableList(cargos);
    }
}
