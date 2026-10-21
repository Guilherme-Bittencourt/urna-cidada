package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.model.RegistroVoto;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApuracaoServiceTest {

    private final ApuracaoService apuracao = new ApuracaoService(DadosEleicao.criarEleicaoSimulada());

    private static RegistroVoto vereador(TipoVoto t, String num, String desc, String sigla) {
        return new RegistroVoto("Vereador", t, num, desc, sigla);
    }

    private static RegistroVoto prefeito(TipoVoto t, String num, String desc, String sigla) {
        return new RegistroVoto("Prefeito", t, num, desc, sigla);
    }

    @Test
    void apuraVereadorComLegendaBrancoENulo() {
        List<RegistroVoto> votos = List.of(
                vereador(TipoVoto.NOMINAL, "42010", "Nina Biblioteca", "PLIV"),
                vereador(TipoVoto.NOMINAL, "42010", "Nina Biblioteca", "PLIV"),
                vereador(TipoVoto.NOMINAL, "31001", "Marcos Guidão", "PBIC"),
                vereador(TipoVoto.LEGENDA, "42999", "Legenda PLIV", "PLIV"),
                vereador(TipoVoto.BRANCO, "", "Branco", null),
                vereador(TipoVoto.NULO, "99999", "Nulo", null),
                prefeito(TipoVoto.NOMINAL, "53", "Lia Batuque", "PMUS"));

        ResultadoCargo r = apuracao.apurar(votos).get(0);
        assertEquals("Vereador", r.cargo());
        assertEquals("42010", r.candidatos().get(0).numero());   // mais votado primeiro
        assertEquals(2, r.candidatos().get(0).votos());
        assertEquals(8, r.candidatos().size());                  // inclui candidatos com 0 voto
        assertEquals(1, r.legenda());
        assertEquals(4, r.validos());
        assertEquals(1, r.brancos());
        assertEquals(1, r.nulos());
        assertEquals(6, r.total());
        assertEquals(50.0, r.percentualValidos(2), 0.001);

        ResultadoCargo.Linha liv = r.partidos().get(0);
        assertEquals("PLIV", liv.siglaPartido());
        assertEquals(3, liv.votos());                            // 2 nominais + 1 legenda
    }

    @Test
    void apuraPrefeitoSeparadamente() {
        List<RegistroVoto> votos = List.of(
                prefeito(TipoVoto.NOMINAL, "53", "Lia Batuque", "PMUS"),
                prefeito(TipoVoto.NOMINAL, "64", "Joana Castanha", "PCAJU"),
                prefeito(TipoVoto.NOMINAL, "53", "Lia Batuque", "PMUS"),
                prefeito(TipoVoto.BRANCO, "", "Branco", null));
        ResultadoCargo r = apuracao.apurar(votos).get(1);
        assertEquals("Prefeito", r.cargo());
        assertEquals("Lia Batuque", r.candidatos().get(0).nome());
        assertEquals(3, r.validos());
        assertEquals(4, r.total());
    }

    @Test
    void semVotosNaoDivideporZero() {
        ResultadoCargo r = apuracao.apurar(List.of()).get(0);
        assertEquals(0, r.total());
        assertEquals(0.0, r.percentualValidos(0));
    }

    @Test
    void boletimTextoTemOsCargos() {
        String texto = apuracao.gerarBoletimTexto(apuracao.apurar(List.of()));
        assertTrue(texto.contains("VEREADOR"));
        assertTrue(texto.contains("PREFEITO"));
    }
}
