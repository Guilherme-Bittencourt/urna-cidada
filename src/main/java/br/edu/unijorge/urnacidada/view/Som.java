package br.edu.unijorge.urnacidada.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sons da urna gerados por síntese simples (sem arquivos de áudio).
 * Se o computador não tiver saída de som, os métodos simplesmente não fazem nada.
 */
public class Som {

    private static final float TAXA = 44_100f;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "som-urna");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean ativo = true;

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    /** "Pi" curto ao apertar uma tecla. */
    public void tecla() {
        tocar(new int[][]{{1000, 70}});
    }

    /** Aviso de tecla inválida (tom grave). */
    public void erro() {
        tocar(new int[][]{{300, 160}});
    }

    /** Confirmação de um cargo (ainda há outros cargos). */
    public void confirma() {
        tocar(new int[][]{{1100, 90}, {0, 40}, {1100, 90}});
    }

    /** O famoso som de fim de votação: bipes rápidos seguidos de um bipe longo. */
    public void fim() {
        int[][] seq = new int[11][];
        for (int i = 0; i < 5; i++) {
            seq[i * 2] = new int[]{1150, 75};
            seq[i * 2 + 1] = new int[]{0, 25};
        }
        seq[10] = new int[]{1150, 650};
        tocar(seq);
    }

    /** @param sequencia pares {frequência em Hz (0 = silêncio), duração em ms} */
    private void tocar(int[][] sequencia) {
        if (!ativo) {
            return;
        }
        executor.execute(() -> {
            try {
                byte[] audio = sintetizar(sequencia);
                AudioFormat formato = new AudioFormat(TAXA, 8, 1, true, false);
                try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                    linha.open(formato);
                    linha.start();
                    linha.write(audio, 0, audio.length);
                    linha.drain();
                }
            } catch (Exception | LinkageError e) {
                // sem dispositivo de áudio (ou ambiente sem som): ignora silenciosamente
            }
        });
    }

    private static byte[] sintetizar(int[][] sequencia) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int[] nota : sequencia) {
            int freq = nota[0];
            int amostras = (int) (TAXA * nota[1] / 1000);
            int rampa = Math.min(200, amostras / 4); // evita "estalos" no início/fim
            for (int i = 0; i < amostras; i++) {
                double env = 1.0;
                if (i < rampa) env = i / (double) rampa;
                else if (i > amostras - rampa) env = (amostras - i) / (double) rampa;
                double valor = freq == 0 ? 0 : Math.signum(Math.sin(2 * Math.PI * freq * i / TAXA)) * 0.25 * env;
                out.write((byte) (valor * 127));
            }
        }
        return out.toByteArray();
    }
}
