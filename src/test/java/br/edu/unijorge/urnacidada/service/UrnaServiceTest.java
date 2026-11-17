package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testes das regras da urna. Os votos vão para uma lista em vez do Firebase. */
class UrnaServiceTest {

    private List<Voto> gravados;
    private UrnaService urna;

    @BeforeEach
    void criarUrna() {
        gravados = new ArrayList<>();
        urna = new UrnaService(DadosEleicao.criarCargos(), gravados::add);
    }

    private void digitar(String numero) {
        for (char c : numero.toCharArray()) {
            urna.digitar(c - '0');
        }
    }

    @Test
    void votoNominalEmVereadorEPrefeito() {
        digitar("42010");
        assertTrue(urna.confirmar());
        digitar("53");
        assertTrue(urna.confirmar());

        assertTrue(urna.isFim());
        assertEquals(2, gravados.size());
        assertEquals("Nina Biblioteca", gravados.get(0).getDescricao());
        assertEquals("Lia Batuque", gravados.get(1).getDescricao());
    }

    @Test
    void partidoExisteMasCandidatoNaoEhVotoDeLegenda() {
        digitar("42999");
        urna.confirmar();
        assertEquals(TipoVoto.LEGENDA, gravados.get(0).getTipo());
    }

    @Test
    void numeroInexistenteEhVotoNulo() {
        digitar("99999");
        urna.confirmar();
        digitar("77");
        urna.confirmar();
        assertEquals(TipoVoto.NULO, gravados.get(0).getTipo());
        assertEquals(TipoVoto.NULO, gravados.get(1).getTipo());
    }

    @Test
    void prefeitoNaoTemVotoDeLegenda() {
        digitar("42010");
        urna.confirmar();
        digitar("4");
        assertFalse(urna.confirmar()); // número incompleto
        urna.corrigir();
        digitar("41");
        urna.confirmar();
        assertEquals(TipoVoto.NULO, gravados.get(1).getTipo());
    }

    @Test
    void brancoSoFuncionaSemNumeroDigitado() {
        digitar("4");
        urna.votarEmBranco();
        assertFalse(urna.isBranco());

        urna.corrigir();
        urna.votarEmBranco();
        assertTrue(urna.confirmar());
        assertEquals(TipoVoto.BRANCO, gravados.get(0).getTipo());
    }

    @Test
    void naoDeixaDigitarMaisQueOTamanhoDoNumero() {
        digitar("4201099");
        assertEquals("42010", urna.getDigitos());
    }
}
