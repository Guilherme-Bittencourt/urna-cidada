package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.CandidatoMajoritario;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.CargoMajoritario;
import br.edu.unijorge.urnacidada.model.Partido;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.model.VotoLegenda;
import br.edu.unijorge.urnacidada.model.VotoNominal;
import br.edu.unijorge.urnacidada.model.VotoNulo;
import br.edu.unijorge.urnacidada.service.UrnaService;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;

/**
 * Tela (display) da urna. Desenha tudo "à mão" com Graphics2D, a partir do estado do {@link UrnaService}.
 */
public class PainelTela extends JPanel {

    private static final Color FUNDO = new Color(0xF3F3EE);
    private static final Color TEXTO = new Color(0x111111);
    private static final Color CINZA = new Color(0x777777);
    private static final Color VERMELHO = new Color(0xC0392B);
    private static final int MARGEM = 26;

    private final UrnaService urna;

    private boolean cursorVisivel = true;
    private boolean gravando;
    private float progressoGravacao;
    private String aviso;
    private int segundosParaProximo = -1;

    private final Timer timerAviso;

    public PainelTela(UrnaService urna) {
        this.urna = urna;
        setBackground(FUNDO);
        setPreferredSize(new Dimension(660, 470));
        setBorder(BorderFactory.createMatteBorder(14, 14, 14, 14, PainelTeclado.COR_CORPO));

        Timer pisca = new Timer(500, e -> {
            cursorVisivel = !cursorVisivel;
            repaint();
        });
        pisca.start();

        timerAviso = new Timer(3000, e -> {
            aviso = null;
            repaint();
        });
        timerAviso.setRepeats(false);
    }

    // ------------------------------------------------------------------ API usada pela janela

    /** Mostra a barra "GRAVANDO" por {@code duracaoMs} e depois executa {@code depois}. */
    public void mostrarGravando(int duracaoMs, Runnable depois) {
        gravando = true;
        progressoGravacao = 0;
        long inicio = System.currentTimeMillis();
        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            progressoGravacao = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) duracaoMs);
            repaint();
            if (progressoGravacao >= 1f) {
                t.stop();
                gravando = false;
                repaint();
                if (depois != null) depois.run();
            }
        });
        t.start();
        repaint();
    }

    public boolean isGravando() { return gravando; }

    /** Mensagem temporária em vermelho no rodapé (ex.: tecla inválida). */
    public void mostrarAviso(String texto) {
        aviso = texto;
        timerAviso.restart();
        repaint();
    }

    /** Contagem regressiva exibida na tela de FIM (-1 = não mostrar). */
    public void setSegundosParaProximo(int segundos) {
        segundosParaProximo = segundos;
        repaint();
    }

    // ------------------------------------------------------------------ desenho

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // área útil (descontando a moldura)
        int ox = getInsets().left;
        int oy = getInsets().top;
        int w = getWidth() - getInsets().left - getInsets().right;
        int h = getHeight() - getInsets().top - getInsets().bottom;
        g2.translate(ox, oy);

        if (gravando) {
            desenharGravando(g2, w, h);
        } else if (urna.getEstado() == UrnaService.Estado.FIM) {
            desenharFim(g2, w, h);
        } else {
            desenharVotacao(g2, w, h);
        }

        if (aviso != null) {
            g2.setFont(fonte(Font.BOLD, 15));
            g2.setColor(VERMELHO);
            centralizar(g2, aviso, w, h - 12);
        }
        g2.dispose();
    }

    private void desenharVotacao(Graphics2D g, int w, int h) {
        Cargo cargo = urna.getCargoAtual().orElseThrow();
        String digitos = urna.getDigitos();
        boolean branco = urna.getEstado() == UrnaService.Estado.BRANCO;
        Optional<Voto> previsto = urna.getVotoPrevisto();

        if (!digitos.isEmpty() || branco) {
            g.setColor(TEXTO);
            g.setFont(fonte(Font.BOLD, 16));
            g.drawString("SEU VOTO PARA", MARGEM, 38);
        }

        g.setColor(TEXTO);
        g.setFont(fonte(Font.BOLD, 32));
        g.drawString(cargo.getNome(), w / 2 - g.getFontMetrics().stringWidth(cargo.getNome()) / 2 - 60, 92);

        if (branco) {
            g.setFont(fonte(Font.BOLD, 46));
            centralizar(g, "VOTO EM BRANCO", w, 245);
        } else {
            desenharNumero(g, cargo, digitos);
            int y = 222;
            if (previsto.isPresent()) {
                Voto v = previsto.get();
                if (v instanceof VotoNominal nominal) {
                    linhaRotulo(g, "Nome:", nominal.getCandidato().getNome(), y);
                    Partido p = nominal.getCandidato().getPartido();
                    linhaRotulo(g, "Partido:", p.getSigla() + " – " + p.getNome(), y + 34);
                    if (nominal.getCandidato() instanceof CandidatoMajoritario maj && cargo instanceof CargoMajoritario cm) {
                        linhaRotulo(g, cm.getNomeCargoVice() + ":", maj.getVice(), y + 68);
                        desenharFoto(g, w - MARGEM - 120, 22, 120, 150, p.getCor(), maj.getIniciais(), cargo.getNome());
                        desenharFoto(g, w - MARGEM - 90, 196, 90, 110, p.getCor().darker(),
                                iniciais(maj.getVice()), "Vice");
                    } else {
                        desenharFoto(g, w - MARGEM - 120, 22, 120, 150, p.getCor(),
                                nominal.getCandidato().getIniciais(), cargo.getNome());
                    }
                } else if (v instanceof VotoLegenda legenda) {
                    Partido p = legenda.getPartido();
                    linhaRotulo(g, "Partido:", p.getSigla() + " – " + p.getNome(), y);
                    g.setFont(fonte(Font.BOLD, 34));
                    g.setColor(TEXTO);
                    centralizar(g, "VOTO DE LEGENDA", w, y + 72);
                } else if (v instanceof VotoNulo) {
                    g.setFont(fonte(Font.BOLD, 22));
                    g.setColor(TEXTO);
                    g.drawString("NÚMERO ERRADO", MARGEM, y);
                    g.setFont(fonte(Font.BOLD, 46));
                    centralizar(g, "VOTO NULO", w, y + 70);
                }
            } else {
                urna.getPartidoDigitado().ifPresent(p ->
                        linhaRotulo(g, "Partido:", p.getSigla() + " – " + p.getNome(), y));
            }
        }

        if (previsto.isPresent()) {
            desenharRodapeConfirmacao(g, w, h);
        } else if (digitos.isEmpty() && aviso == null) {
            g.setFont(fonte(Font.PLAIN, 15));
            g.setColor(CINZA);
            centralizar(g, "Digite o número do candidato ou aperte BRANCO", w, h - 30);
        }
    }

    private void desenharNumero(Graphics2D g, Cargo cargo, String digitos) {
        g.setColor(TEXTO);
        g.setFont(fonte(Font.PLAIN, 18));
        g.drawString("Número:", MARGEM, 160);

        int caixa = 40;
        int altura = 52;
        int x0 = MARGEM + 90;
        int y0 = 122;
        g.setStroke(new BasicStroke(1.6f));
        for (int i = 0; i < cargo.getQuantidadeDigitos(); i++) {
            int x = x0 + i * (caixa + 8);
            g.setColor(TEXTO);
            g.drawRect(x, y0, caixa, altura);
            if (i < digitos.length()) {
                g.setFont(fonte(Font.BOLD, 34));
                String d = String.valueOf(digitos.charAt(i));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(d, x + (caixa - fm.stringWidth(d)) / 2, y0 + altura / 2 + fm.getAscent() / 2 - 4);
            } else if (i == digitos.length() && cursorVisivel) {
                g.fillRect(x + 3, y0 + 3, caixa - 5, altura - 5);
            }
        }
    }

    private void desenharRodapeConfirmacao(Graphics2D g, int w, int h) {
        int yLinha = h - 92;
        g.setColor(TEXTO);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(0, yLinha, w, yLinha);

        g.setFont(fonte(Font.PLAIN, 15));
        g.drawString("Aperte a tecla:", MARGEM, yLinha + 24);
        teclaRodape(g, "CONFIRMA", PainelTeclado.COR_CONFIRMA, "para CONFIRMAR este voto", yLinha + 48);
        teclaRodape(g, "CORRIGE", PainelTeclado.COR_CORRIGE, "para REINICIAR este voto", yLinha + 72);
    }

    private void teclaRodape(Graphics2D g, String tecla, Color cor, String texto, int y) {
        int x = MARGEM + 30;
        g.setFont(fonte(Font.BOLD, 15));
        g.setColor(cor.darker());
        g.drawString(tecla, x, y);
        g.setColor(TEXTO);
        g.setFont(fonte(Font.PLAIN, 15));
        g.drawString(texto, x + 100, y);
    }

    private void desenharFim(Graphics2D g, int w, int h) {
        g.setColor(TEXTO);
        g.setFont(fonte(Font.BOLD, 150));
        centralizar(g, "FIM", w, h / 2 + 40);
        g.setFont(fonte(Font.PLAIN, 16));
        g.setColor(CINZA);
        String msg = segundosParaProximo > 0
                ? "Obrigado por participar! Próximo eleitor em " + segundosParaProximo + "s"
                : "Obrigado por participar! Aguarde o mesário liberar a urna.";
        centralizar(g, msg, w, h - 40);
    }

    private void desenharGravando(Graphics2D g, int w, int h) {
        int bw = (int) (w * 0.6);
        int bh = 26;
        int x = (w - bw) / 2;
        int y = h / 2 - bh / 2;
        g.setColor(TEXTO);
        g.setFont(fonte(Font.BOLD, 20));
        centralizar(g, "GRAVANDO", w, y - 18);
        g.setColor(new Color(0xDDDDDD));
        g.fillRect(x, y, bw, bh);
        g.setColor(PainelTeclado.COR_CONFIRMA);
        g.fillRect(x, y, (int) (bw * progressoGravacao), bh);
        g.setColor(TEXTO);
        g.drawRect(x, y, bw, bh);
    }

    // ------------------------------------------------------------------ utilitários de desenho

    private void linhaRotulo(Graphics2D g, String rotulo, String valor, int y) {
        g.setColor(TEXTO);
        g.setFont(fonte(Font.PLAIN, 18));
        g.drawString(rotulo, MARGEM, y);
        g.setFont(fonte(Font.BOLD, 20));
        g.drawString(valor, MARGEM + g.getFontMetrics(fonte(Font.PLAIN, 18)).stringWidth(rotulo) + 10, y);
    }

    /** Desenha um "retrato" ilustrativo (os candidatos são fictícios, então não há foto real). */
    private void desenharFoto(Graphics2D g, int x, int y, int w, int h, Color cor, String iniciais, String legenda) {
        g.setColor(new Color(0xE2E2DC));
        g.fillRect(x, y, w, h);
        // silhueta
        g.setColor(cor);
        int cabeca = w / 2;
        g.fillOval(x + (w - cabeca) / 2, y + h / 8, cabeca, cabeca);
        g.fillRoundRect(x + w / 8, y + h / 8 + cabeca + 4, w * 3 / 4, h - (h / 8 + cabeca + 4), w / 2, w / 2);
        g.setColor(Color.WHITE);
        g.setFont(fonte(Font.BOLD, Math.max(12, w / 6)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(iniciais, x + (w - fm.stringWidth(iniciais)) / 2,
                y + h / 8 + cabeca / 2 + fm.getAscent() / 2 - 2);
        g.setColor(TEXTO);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(x, y, w, h);
        g.setFont(fonte(Font.PLAIN, 12));
        fm = g.getFontMetrics();
        g.drawString(legenda, x + (w - fm.stringWidth(legenda)) / 2, y + h + 15);
    }

    private static void centralizar(Graphics2D g, String texto, int largura, int y) {
        g.drawString(texto, (largura - g.getFontMetrics().stringWidth(texto)) / 2, y);
    }

    private static Font fonte(int estilo, int tamanho) {
        return new Font(Font.SANS_SERIF, estilo, tamanho);
    }

    private static String iniciais(String nome) {
        String[] p = nome.trim().split("\\s+");
        return (p[0].substring(0, 1) + (p.length > 1 ? p[p.length - 1].substring(0, 1) : "")).toUpperCase();
    }
}
