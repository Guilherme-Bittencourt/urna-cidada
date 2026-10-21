package br.edu.unijorge.urnacidada.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.IntConsumer;

/**
 * Teclado da urna: teclas de 0 a 9 e as teclas BRANCO, CORRIGE e CONFIRMA.
 * O painel não conhece as regras da urna: ele só avisa quem foi apertado.
 */
public class PainelTeclado extends JPanel {

    public static final Color COR_CORPO = new Color(0x2A2A2A);
    public static final Color COR_BRANCO = new Color(0xF4F4F4);
    public static final Color COR_CORRIGE = new Color(0xE67E22);
    public static final Color COR_CONFIRMA = new Color(0x1E9E4A);

    private final BotaoUrna[] digitos = new BotaoUrna[10];
    private final BotaoUrna botaoBranco;
    private final BotaoUrna botaoCorrige;
    private final BotaoUrna botaoConfirma;

    public PainelTeclado(IntConsumer aoDigitar, Runnable aoBranco, Runnable aoCorrigir, Runnable aoConfirmar) {
        super(new BorderLayout(0, 16));
        setBackground(COR_CORPO);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));
        setPreferredSize(new Dimension(330, 470));

        JPanel topo = new JPanel(new GridLayout(2, 1));
        topo.setOpaque(false);
        JLabel titulo = new JLabel("URNA CIDADÃ", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JLabel subtitulo = new JLabel("simulador educativo", SwingConstants.CENTER);
        subtitulo.setForeground(new Color(0xAAAAAA));
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        topo.add(titulo);
        topo.add(subtitulo);
        add(topo, BorderLayout.NORTH);

        Font fonteDigito = new Font(Font.SANS_SERIF, Font.BOLD, 26);
        JPanel numeros = new JPanel(new GridLayout(4, 3, 12, 12));
        numeros.setOpaque(false);
        for (int i = 0; i <= 9; i++) {
            final int n = i;
            digitos[i] = new BotaoUrna(String.valueOf(i), new Color(0x111111), Color.WHITE, fonteDigito);
            digitos[i].addActionListener(e -> aoDigitar.accept(n));
        }
        for (int i = 1; i <= 9; i++) {
            numeros.add(digitos[i]);
        }
        numeros.add(espaco());
        numeros.add(digitos[0]);
        numeros.add(espaco());
        add(numeros, BorderLayout.CENTER);

        Font fonteAcao = new Font(Font.SANS_SERIF, Font.BOLD, 13);
        botaoBranco = new BotaoUrna("BRANCO", COR_BRANCO, Color.BLACK, fonteAcao);
        botaoCorrige = new BotaoUrna("CORRIGE", COR_CORRIGE, Color.BLACK, fonteAcao);
        botaoConfirma = new BotaoUrna("CONFIRMA", COR_CONFIRMA, Color.BLACK, fonteAcao);
        botaoBranco.addActionListener(e -> aoBranco.run());
        botaoCorrige.addActionListener(e -> aoCorrigir.run());
        botaoConfirma.addActionListener(e -> aoConfirmar.run());

        JPanel acoes = new JPanel(new GridLayout(1, 3, 10, 0));
        acoes.setOpaque(false);
        acoes.setPreferredSize(new Dimension(0, 64));
        acoes.add(botaoBranco);
        acoes.add(botaoCorrige);
        acoes.add(botaoConfirma);
        add(acoes, BorderLayout.SOUTH);
    }

    private static JPanel espaco() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    /** "Aperta" visualmente a tecla, usado quando o eleitor usa o teclado do computador. */
    public void pressionarDigito(int n) { digitos[n].doClick(90); }
    public void pressionarBranco() { botaoBranco.doClick(90); }
    public void pressionarCorrige() { botaoCorrige.doClick(90); }
    public void pressionarConfirma() { botaoConfirma.doClick(90); }
}
