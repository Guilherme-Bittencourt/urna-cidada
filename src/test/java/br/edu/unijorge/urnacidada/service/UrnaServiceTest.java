package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.model.RegistroVoto;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.model.VotoLegenda;
import br.edu.unijorge.urnacidada.model.VotoNominal;
import br.edu.unijorge.urnacidada.model.VotoNulo;
import br.edu.unijorge.urnacidada.repository.MemoriaVotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrnaServiceTest {

    private MemoriaVotoRepository repo;
    private UrnaService urna;

    @BeforeEach
    void preparar() {
        repo = new MemoriaVotoRepository();
        // Runnable::run -> grava na hora (sem thread), para o teste ser determinístico
        urna = new UrnaService(DadosEleicao.criarEleicaoSimulada(), repo, Runnable::run);
    }

    private void digitar(String numero) {
        for (char c : numero.toCharArray()) {
            urna.digitar(c - '0');
        }
    }

    @Test
    void comecaPeloVereador() {
        assertEquals("Vereador", urna.getCargoAtual().orElseThrow().getNome());
        assertEquals(UrnaService.Estado.DIGITANDO, urna.getEstado());
    }

    @Test
    void votoNominalNoVereadorEPrefeito() {
        digitar("42010");
        Voto v1 = urna.confirmar().orElseThrow();
        assertInstanceOf(VotoNominal.class, v1);
        assertEquals("Nina Biblioteca", v1.getDescricao());
        assertEquals("Prefeito", urna.getCargoAtual().orElseThrow().getNome());

        digitar("53");
        Voto v2 = urna.confirmar().orElseThrow();
        assertEquals("Lia Batuque", v2.getDescricao());
        assertEquals(UrnaService.Estado.FIM, urna.getEstado());
        assertEquals(2, repo.listarTodos().size());
        assertEquals(1, urna.getEleitoresQueVotaram());
    }

    @Test
    void partidoExisteMasCandidatoNaoEhVotoDeLegenda() {
        digitar("31999");
        Voto v = urna.getVotoPrevisto().orElseThrow();
        assertInstanceOf(VotoLegenda.class, v);
        assertEquals("PBIC", v.getSiglaPartido());
        assertTrue(v.isValido());
    }

    @Test
    void numeroSemPartidoEhNuloNoVereador() {
        digitar("99999");
        assertInstanceOf(VotoNulo.class, urna.getVotoPrevisto().orElseThrow());
    }

    @Test
    void prefeitoInexistenteEhNulo() {
        digitar("31001");
        urna.confirmar();
        digitar("77");
        Voto v = urna.confirmar().orElseThrow();
        assertEquals(TipoVoto.NULO, v.getTipo());
        assertFalse(v.isValido());
    }

    @Test
    void mostraPartidoEnquantoDigita() {
        digitar("6");
        assertTrue(urna.getPartidoDigitado().isEmpty());
        digitar("4");
        assertEquals("PCAJU", urna.getPartidoDigitado().orElseThrow().getSigla());
    }

    @Test
    void naoConfirmaNumeroIncompleto() {
        digitar("420");
        assertTrue(urna.confirmar().isEmpty());
        assertEquals("Vereador", urna.getCargoAtual().orElseThrow().getNome());
        assertTrue(repo.listarTodos().isEmpty());
    }

    @Test
    void ignoraDigitoAlemDoLimite() {
        digitar("42010");
        assertFalse(urna.digitar(7));
        assertEquals("42010", urna.getDigitos());
    }

    @Test
    void corrigeApagaOsDigitos() {
        digitar("4201");
        urna.corrigir();
        assertEquals("", urna.getDigitos());
        digitar("53333");
        assertEquals("Zeca Tambor", urna.getVotoPrevisto().orElseThrow().getDescricao());
    }

    @Test
    void votoEmBranco() {
        assertTrue(urna.votarEmBranco());
        assertEquals(TipoVoto.BRANCO, urna.confirmar().orElseThrow().getTipo());
    }

    @Test
    void brancoNaoFuncionaDepoisDeDigitar() {
        digitar("4");
        assertFalse(urna.votarEmBranco());
        urna.corrigir();
        assertTrue(urna.votarEmBranco());
    }

    @Test
    void naoDigitaDepoisDeEscolherBranco() {
        urna.votarEmBranco();
        assertFalse(urna.digitar(1));
    }

    @Test
    void fimBloqueiaAteLiberarProximoEleitor() {
        urna.votarEmBranco();
        urna.confirmar();
        urna.votarEmBranco();
        urna.confirmar();
        assertEquals(UrnaService.Estado.FIM, urna.getEstado());
        assertFalse(urna.digitar(1));
        assertTrue(urna.confirmar().isEmpty());

        urna.liberarProximoEleitor();
        assertEquals("Vereador", urna.getCargoAtual().orElseThrow().getNome());
    }

    @Test
    void votoNaoGuardaDadosDoEleitor() {
        digitar("42010");
        urna.confirmar();
        RegistroVoto r = repo.listarTodos().get(0);
        assertEquals(new RegistroVoto("Vereador", TipoVoto.NOMINAL, "42010", "Nina Biblioteca", "PLIV"), r);
    }

    @Test
    void falhaNaGravacaoAvisaListener() {
        MemoriaVotoRepository quebrado = new MemoriaVotoRepository() {
            @Override
            public void salvar(RegistroVoto voto) {
                throw new IllegalStateException("sem internet");
            }
        };
        UrnaService u = new UrnaService(DadosEleicao.criarEleicaoSimulada(), quebrado, Runnable::run);
        final String[] erro = {null};
        u.adicionarListener(new UrnaListener() {
            @Override
            public void erroAoGravar(Voto voto, Exception e) {
                erro[0] = e.getMessage();
            }
        });
        u.votarEmBranco();
        u.confirmar();
        assertEquals("sem internet", erro[0]);
    }

    @Test
    void variosEleitores() {
        for (int i = 0; i < 3; i++) {
            digitar("31001");
            urna.confirmar();
            digitar("31");
            urna.confirmar();
            urna.liberarProximoEleitor();
        }
        List<RegistroVoto> votos = repo.listarTodos();
        assertEquals(6, votos.size());
        assertEquals(3, urna.getEleitoresQueVotaram());
    }
}
