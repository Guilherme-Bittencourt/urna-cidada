package br.edu.unijorge.urnacidada.view;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Botão com o visual das teclas da urna (HERANÇA de JButton, sobrescrevendo o desenho).
 */
public class BotaoUrna extends JButton {

    private final Color fundo;
    private final Color corTexto;

    public BotaoUrna(String texto, Color fundo, Color corTexto, Font fonte) {
        super(texto);
        this.fundo = fundo;
        this.corTexto = corTexto;
        setFont(fonte);
        setFocusable(false);          // o teclado físico é tratado pela janela
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean pressionado = getModel().isArmed() && getModel().isPressed();
        int desloc = pressionado ? 2 : 0;
        int w = getWidth() - 4;
        int h = getHeight() - 5;
        int arco = 10;

        // sombra (dá a sensação de tecla física)
        if (!pressionado) {
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRoundRect(2, 4, w, h, arco, arco);
        }

        Color cor = fundo;
        if (!isEnabled()) cor = fundo.darker();
        else if (pressionado) cor = fundo.darker();
        else if (getModel().isRollover()) cor = clarear(fundo);
        g2.setColor(cor);
        g2.fillRoundRect(desloc, desloc, w, h, arco, arco);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(desloc, desloc, w - 1, h - 1, arco, arco);

        g2.setFont(getFont());
        g2.setColor(corTexto);
        FontMetrics fm = g2.getFontMetrics();
        String texto = getText();
        int x = desloc + (w - fm.stringWidth(texto)) / 2;
        int y = desloc + (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, x, y);
        g2.dispose();
    }

    private static Color clarear(Color c) {
        return new Color(Math.min(255, c.getRed() + 25), Math.min(255, c.getGreen() + 25), Math.min(255, c.getBlue() + 25));
    }
}
