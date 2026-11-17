package br.edu.unijorge.urnacidada.data;

import br.edu.unijorge.urnacidada.model.CandidatoMajoritario;
import br.edu.unijorge.urnacidada.model.CandidatoProporcional;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.CargoMajoritario;
import br.edu.unijorge.urnacidada.model.CargoProporcional;
import br.edu.unijorge.urnacidada.model.Partido;

import java.util.List;

/** Partidos e candidatos FICTÍCIOS da eleição simulada. */
public class DadosEleicao {

    public static List<Cargo> criarCargos() {
        Partido bicicletas = new Partido(31, "PBIC", "Partido das Bicicletas");
        Partido livros = new Partido(42, "PLIV", "Partido dos Livros");
        Partido musica = new Partido(53, "PMUS", "Partido da Música");
        Partido caju = new Partido(64, "PCAJU", "Partido do Pé de Caju");

        Cargo vereador = new CargoProporcional("Vereador");
        vereador.adicionarCandidato(new CandidatoProporcional("31001", "Marcos Guidão", bicicletas));
        vereador.adicionarCandidato(new CandidatoProporcional("31123", "Teca Ciclovia", bicicletas));
        vereador.adicionarCandidato(new CandidatoProporcional("42010", "Nina Biblioteca", livros));
        vereador.adicionarCandidato(new CandidatoProporcional("42222", "Hugo Poesia", livros));
        vereador.adicionarCandidato(new CandidatoProporcional("53333", "Zeca Tambor", musica));
        vereador.adicionarCandidato(new CandidatoProporcional("53050", "Mel Melodia", musica));
        vereador.adicionarCandidato(new CandidatoProporcional("64444", "Cacau Pomar", caju));
        vereador.adicionarCandidato(new CandidatoProporcional("64007", "Biu Semente", caju));

        Cargo prefeito = new CargoMajoritario("Prefeito");
        prefeito.adicionarCandidato(new CandidatoMajoritario("31", "Clara Pedal", bicicletas, "Otávio Roda"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("42", "Bento Leitura", livros, "Sofia Página"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("53", "Lia Batuque", musica, "Davi Pandeiro"));
        prefeito.adicionarCandidato(new CandidatoMajoritario("64", "Joana Castanha", caju, "Rui Mangueira"));

        // a urna pede os votos nesta ordem: primeiro vereador, depois prefeito
        return List.of(vereador, prefeito);
    }
}
