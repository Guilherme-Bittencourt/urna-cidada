package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.Partido;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.service.UrnaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/** Janela da urna (Java Swing): tela à esquerda e teclado à direita. HERANÇA de JFrame. */
public class TelaUrna extends JFrame {

    private final UrnaService urna;

    private final JLabel lblCargo = new JLabel();
    private final JLabel lblNumero = new JLabel();
    private final JLabel lblNome = new JLabel();
    private final JLabel lblPartido = new JLabel();
    private final JLabel lblExtra = new JLabel();
    private final JLabel lblAviso = new JLabel();
    private final JLabel lblInstrucao = new JLabel();

    public TelaUrna(UrnaService urna) {
        super("Urna Cidadã");
        this.urna = urna;

        setLayout(new BorderLayout(10, 10));
        add(criarTela(), BorderLayout.CENTER);
        add(criarTeclado(), BorderLayout.EAST);

        setSize(900, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        atualizarTela();
    }

    private JPanel criarTela() {
        JPanel tela = new JPanel(new GridLayout(7, 1));
        tela.setBackground(Color.WHITE);
        tela.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblCargo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblNumero.setFont(new Font("Monospaced", Font.BOLD, 34));
        lblAviso.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblAviso.setForeground(Color.RED);
        for (JLabel l : new JLabel[]{lblNome, lblPartido, lblExtra}) {
            l.setFont(new Font("SansSerif", Font.PLAIN, 20));
        }

        tela.add(lblCargo);
        tela.add(lblNumero);
        tela.add(lblNome);
        tela.add(lblPartido);
        tela.add(lblExtra);
        tela.add(lblAviso);
        tela.add(lblInstrucao);
        return tela;
    }

    private JPanel criarTeclado() {
        JPanel teclado = new JPanel(new GridLayout(5, 3, 6, 6));
        teclado.setBackground(Color.DARK_GRAY);
        teclado.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int n = 1; n <= 9; n++) {
            teclado.add(criarTeclaNumero(n));
        }
        teclado.add(new JLabel());
        teclado.add(criarTeclaNumero(0));
        teclado.add(new JLabel());

        teclado.add(criarBotao("BRANCO", Color.WHITE, e -> { urna.votarEmBranco(); atualizarTela(); }));
        teclado.add(criarBotao("CORRIGE", Color.ORANGE, e -> { urna.corrigir(); atualizarTela(); }));
        teclado.add(criarBotao("CONFIRMA", Color.GREEN, e -> confirmar()));
        return teclado;
    }

    private JButton criarTeclaNumero(int n) {
        JButton b = criarBotao(String.valueOf(n), Color.BLACK, e -> { urna.digitar(n); atualizarTela(); });
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 22));
        return b;
    }

    private JButton criarBotao(String texto, Color cor, java.awt.event.ActionListener acao) {
        JButton b = new JButton(texto);
        b.setBackground(cor);
        b.setFocusPainted(false);
        b.addActionListener(acao);
        return b;
    }

    private void confirmar() {
        try {
            urna.confirmar();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        atualizarTela();

        if (urna.isFim()) {
            // depois de 3 segundos a urna libera o próximo eleitor
            Timer timer = new Timer(3000, e -> { urna.proximoEleitor(); atualizarTela(); });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /** Redesenha a tela de acordo com o estado da urna. */
    private void atualizarTela() {
        lblNumero.setText("");
        lblNome.setText("");
        lblPartido.setText("");
        lblExtra.setText("");
        lblAviso.setText("");
        lblInstrucao.setText("");

        if (urna.isFim()) {
            lblCargo.setText("FIM");
            return;
        }

        Cargo cargo = urna.getCargoAtual();
        lblCargo.setText(cargo.getNome());

        if (urna.isBranco()) {
            lblAviso.setText("VOTO EM BRANCO");
            lblInstrucao.setText("Aperte CONFIRMA ou CORRIGE");
            return;
        }

        // número com "_" nas casas que faltam (ex.: 42_ _ _)
        String numero = urna.getDigitos() + " _".repeat(cargo.getQuantidadeDigitos() - urna.getDigitos().length());
        lblNumero.setText("Número: " + numero);

        Partido partido = cargo.buscarPartido(urna.getDigitos());
        if (partido != null) {
            lblPartido.setText("Partido: " + partido.getSigla() + " - " + partido.getNome());
        }

        Voto voto = urna.getVotoAtual();
        if (voto == null) {
            return; // ainda digitando
        }
        if (voto.getTipo() == TipoVoto.NOMINAL) {
            Candidato candidato = cargo.buscarCandidato(urna.getDigitos());
            lblNome.setText("Nome: " + candidato.getNome());
            lblExtra.setText(candidato.getInfoExtra());
        } else if (voto.getTipo() == TipoVoto.LEGENDA) {
            lblAviso.setText("VOTO DE LEGENDA");
        } else {
            lblPartido.setText("");
            lblAviso.setText("NÚMERO ERRADO - VOTO NULO");
        }
        lblInstrucao.setText("Aperte CONFIRMA ou CORRIGE");
    }
}
