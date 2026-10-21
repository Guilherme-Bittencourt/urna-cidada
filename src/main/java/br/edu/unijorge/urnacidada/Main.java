package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.config.FirebaseConfig;
import br.edu.unijorge.urnacidada.data.DadosEleicao;
import br.edu.unijorge.urnacidada.model.Eleicao;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import br.edu.unijorge.urnacidada.service.UrnaService;
import br.edu.unijorge.urnacidada.view.TelaUrna;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do simulador "Urna Cidadã".
 *
 * Arquitetura em camadas:
 * <ul>
 *   <li>model      – entidades (Partido, Candidato, Cargo, Voto...)</li>
 *   <li>data       – dados fictícios da eleição simulada</li>
 *   <li>repository – persistência (Firebase ou memória)</li>
 *   <li>service    – regras da urna e apuração</li>
 *   <li>view       – interface gráfica em Java Swing</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorado) {
            // fica com o visual padrão
        }

        Eleicao eleicao = DadosEleicao.criarEleicaoSimulada();
        VotoRepository repositorio = FirebaseConfig.criarRepositorio();
        UrnaService urna = new UrnaService(eleicao, repositorio);

        SwingUtilities.invokeLater(() -> new TelaUrna(urna).setVisible(true));
    }
}
