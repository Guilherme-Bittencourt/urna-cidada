package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.repository.FirestoreVotoRepository;
import br.edu.unijorge.urnacidada.repository.MemoriaVotoRepository;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import br.edu.unijorge.urnacidada.service.UrnaService;
import br.edu.unijorge.urnacidada.view.TelaUrna;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        VotoRepository repositorio;
        String titulo = "Urna Cidadã";
        try {
            repositorio = new FirestoreVotoRepository("serviceAccountKey.json");
        } catch (Exception e) {
            // sem a chave do Firebase, a urna funciona mesmo assim (votos só na memória)
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao Firebase.\n"
                            + "A urna vai funcionar em MODO OFFLINE (votos só na memória).\n\n"
                            + "Para gravar no Firebase, coloque o serviceAccountKey.json na pasta do projeto.",
                    "Urna Cidadã", JOptionPane.WARNING_MESSAGE);
            repositorio = new MemoriaVotoRepository();
            titulo += " - MODO OFFLINE";
        }

        UrnaService urna = new UrnaService(DadosEleicao.criarCargos(), repositorio);
        String tituloFinal = titulo;
        SwingUtilities.invokeLater(() -> {
            TelaUrna tela = new TelaUrna(urna);
            tela.setTitle(tituloFinal);
            tela.setVisible(true);
        });
    }
}
