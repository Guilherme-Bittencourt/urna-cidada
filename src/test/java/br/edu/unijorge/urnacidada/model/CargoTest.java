package br.edu.unijorge.urnacidada.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CargoTest {

    private final Partido p = new Partido(31, "PBIC", "Partido das Bicicletas", Color.BLUE);

    @Test
    void cargoMajoritarioNaoAceitaCandidatoProporcional() {
        CargoMajoritario prefeito = new CargoMajoritario("Prefeito", "Vice-prefeito");
        assertThrows(IllegalArgumentException.class,
                () -> prefeito.adicionarCandidato(new CandidatoProporcional("31001", "Fulano", p)));
    }

    @Test
    void numeroDoCandidatoPrecisaComecarComOPartido() {
        assertThrows(IllegalArgumentException.class, () -> new CandidatoProporcional("42001", "Fulano", p));
    }

    @Test
    void naoPermiteNumeroRepetido() {
        CargoProporcional vereador = new CargoProporcional("Vereador");
        vereador.adicionarCandidato(new CandidatoProporcional("31001", "A", p));
        assertThrows(IllegalArgumentException.class,
                () -> vereador.adicionarCandidato(new CandidatoProporcional("31001", "B", p)));
    }

    @Test
    void polimorfismoNaInterpretacao() {
        Cargo vereador = new CargoProporcional("Vereador");
        Cargo prefeito = new CargoMajoritario("Prefeito", "Vice-prefeito");
        vereador.adicionarCandidato(new CandidatoProporcional("31001", "A", p));
        prefeito.adicionarCandidato(new CandidatoMajoritario("31", "B", p, "C"));

        // mesmo método, comportamentos diferentes:
        assertInstanceOf(VotoLegenda.class, vereador.interpretar("31555"));
        assertInstanceOf(VotoNulo.class, prefeito.interpretar("32"));
        assertInstanceOf(VotoNominal.class, prefeito.interpretar("31"));
    }

    @Test
    void interpretarExigeQuantidadeCertaDeDigitos() {
        Cargo prefeito = new CargoMajoritario("Prefeito", "Vice-prefeito");
        assertThrows(IllegalArgumentException.class, () -> prefeito.interpretar("123"));
    }
}
