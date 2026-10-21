package br.edu.unijorge.urnacidada.data;

import br.edu.unijorge.urnacidada.model.CandidatoMajoritario;
import br.edu.unijorge.urnacidada.model.CandidatoProporcional;
import br.edu.unijorge.urnacidada.model.CargoMajoritario;
import br.edu.unijorge.urnacidada.model.CargoProporcional;
import br.edu.unijorge.urnacidada.model.Eleicao;
import br.edu.unijorge.urnacidada.model.Partido;

import java.awt.Color;

/**
 * Dados da eleição simulada.
 *
 * TODOS os partidos e candidatos são FICTÍCIOS e temáticos, sem nenhum vínculo
 * com legendas ou pessoas reais (o simulador é apartidário).
 *
 * Para simular uma eleição geral (deputado + presidente), basta trocar os
 * nomes dos cargos aqui: as regras da urna continuam as mesmas.
 */
public final class DadosEleicao {

    private DadosEleicao() {
    }

    public static Eleicao criarEleicaoSimulada() {
        Partido bicicletas = new Partido(31, "PBIC", "Partido das Bicicletas", new Color(0x2E86C1));
        Partido livros     = new Partido(42, "PLIV", "Partido dos Livros", new Color(0x8E44AD));
        Partido musica     = new Partido(53, "PMUS", "Partido da Música", new Color(0xD35400));
        Partido caju       = new Partido(64, "PCAJU", "Partido do Pé de Caju", new Color(0x27AE60));

        // 1º cargo: proporcional (5 dígitos) — a urna real também começa pelo proporcional
        CargoProporcional vereador = new CargoProporcional("Vereador");
        vereador.adicionarCandidato(new CandidatoProporcional("31001", "Marcos Guidão", bicicletas));
        vereador.adicionarCandidato(new CandidatoProporcional("31123", "Teca Ciclovia", bicicletas));
        vereador.adicionarCandidato(new CandidatoProporcional("42010", "Nina Biblioteca", livros));
        vereador.adicionarCandidato(new CandidatoProporcional("42222", "Hugo Poesia", livros));
        vereador.adicionarCandidato(new CandidatoProporcional("53333", "Zeca Tambor", musica));
        vereador.adicionarCandidato(new CandidatoProporcional("53050", "Mel Melodia", musica));
        vereador.adicionarCandidato(new CandidatoProporcional("64444", "Cacau Pomar", caju));
        vereador.adicionarCandidato(new CandidatoProporcional("64007", "Biu Semente", caju));

        // 2º cargo: majoritário (2 dígitos, com vice)
        CargoMajoritario prefeito = new CargoMajoritario("Prefeito", "Vice-prefeito");
        prefeito.adicionarCandidato(new CandidatoMajoritario("31", "Clara Pedal", bicicletas, "Otávio Roda"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("42", "Bento Leitura", livros, "Sofia Página"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("53", "Lia Batuque", musica, "Davi Pandeiro"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("64", "Joana Castanha", caju, "Rui Mangueira"));

        return new Eleicao("Eleição Simulada – Urna Cidadã")
                .adicionarCargo(vereador)
                .adicionarCargo(prefeito);
    }
}
