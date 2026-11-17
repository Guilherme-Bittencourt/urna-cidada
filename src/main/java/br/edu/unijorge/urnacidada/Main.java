package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.repository.FirestoreVotoRepository;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import br.edu.unijorge.urnacidada.service.UrnaService;
import br.edu.unijorge.urnacidada.view.TelaUrna;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        VotoRepository repositorio;
        try {
            repositorio = new FirestoreVotoRepository("serviceAccountKey.json");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao Firebase.\n"
                            + "Coloque o arquivo serviceAccountKey.json na pasta do projeto.\n\n" + e.getMessage(),
                    "Urna Cidadã", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UrnaService urna = new UrnaService(DadosEleicao.criarCargos(), repositorio);
        SwingUtilities.invokeLater(() -> new TelaUrna(urna).setVisible(true));
    }
}
