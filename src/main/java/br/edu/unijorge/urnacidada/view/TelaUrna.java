package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.CandidatoMajoritario;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.repository.MemoriaVotoRepository;
import br.edu.unijorge.urnacidada.service.ApuracaoService;
import br.edu.unijorge.urnacidada.service.UrnaListener;
import br.edu.unijorge.urnacidada.service.UrnaService;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Janela principal: junta a tela, o teclado, o menu do mesário e a barra de status.
 * Recebe as teclas, repassa ao {@link UrnaService} e reage aos avisos dele (Observer).
 */
public class TelaUrna extends JFrame implements UrnaListener {

    private static final int SEGUNDOS_PROXIMO_ELEITOR = 5;

    private final UrnaService urna;
    private final ApuracaoService apuracao;
    private final Som som = new Som();
    private final PainelTela tela;
    private final PainelTeclado teclado;

    private final JLabel statusBanco = new JLabel();
    private final JLabel statusContagem = new JLabel();
    private final AtomicInteger votosGravados = new AtomicInteger();

    private final JCheckBoxMenuItem liberarAutomatico = new JCheckBoxMenuItem("Liberar próximo eleitor automaticamente", true);
    private Timer timerProximo;

    public TelaUrna(UrnaService urna) {
        super("Urna Cidadã – Simulador Educativo de Votação");
        this.urna = urna;
        this.apuracao = new ApuracaoService(urna.getEleicao());

        tela = new PainelTela(urna);
        teclado = new PainelTeclado(this::teclaNumero, this::teclaBranco, this::teclaCorrige, this::teclaConfirma);

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setBackground(PainelTeclado.COR_CORPO);
        corpo.add(tela, BorderLayout.CENTER);
        corpo.add(teclado, BorderLayout.EAST);

        JPanel barraStatus = new JPanel(new BorderLayout());
        barraStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        barraStatus.add(statusBanco, BorderLayout.WEST);
        barraStatus.add(statusContagem, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(corpo, BorderLayout.CENTER);
        add(barraStatus, BorderLayout.SOUTH);
        setJMenuBar(criarMenu());
        configurarAtalhos();

        atualizarStatus();
        urna.adicionarListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ teclas

    private boolean entradaBloqueada() {
        return tela.isGravando() || urna.getEstado() == UrnaService.Estado.FIM;
    }

    private void teclaNumero(int n) {
        if (entradaBloqueada()) return;
        if (urna.digitar(n)) {
            som.tecla();
        } else {
            som.erro();
            if (urna.getEstado() == UrnaService.Estado.BRANCO) {
                tela.mostrarAviso("Voto em branco selecionado. Aperte CONFIRMA ou CORRIGE.");
            } else {
                tela.mostrarAviso("Número completo. Aperte CONFIRMA ou CORRIGE.");
            }
        }
    }

    private void teclaBranco() {
        if (entradaBloqueada()) return;
        if (urna.votarEmBranco()) {
            som.tecla();
        } else {
            som.erro();
            tela.mostrarAviso("Para votar em BRANCO, aperte CORRIGE antes.");
        }
    }

    private void teclaCorrige() {
        if (entradaBloqueada()) return;
        urna.corrigir();
        som.tecla();
    }

    private void teclaConfirma() {
        if (entradaBloqueada()) return;
        if (urna.confirmar().isEmpty()) {
            som.erro();
            tela.mostrarAviso("Digite o número completo ou aperte BRANCO antes de CONFIRMAR.");
        }
        // o restante (som, animação "GRAVANDO", FIM) acontece em votoConfirmado()
    }

    // ------------------------------------------------------------------ avisos do UrnaService

    @Override
    public void estadoAlterado() {
        SwingUtilities.invokeLater(tela::repaint);
    }

    @Override
    public void votoConfirmado(Voto voto) {
        // chamado na thread do Swing (o CONFIRMA veio de um botão), logo antes de a urna avançar de cargo
        boolean acabou = ultimoCargo(voto.getCargo());
        tela.mostrarGravando(acabou ? 900 : 500, () -> {
            if (urna.getEstado() == UrnaService.Estado.FIM) {
                som.fim();
                atualizarStatus();
                agendarProximoEleitor();
            } else {
                som.confirma();
            }
        });
    }

    @Override
    public void votoGravado(Voto voto) {
        votosGravados.incrementAndGet();
        SwingUtilities.invokeLater(this::atualizarStatus);
    }

    @Override
    public void erroAoGravar(Voto voto, Exception erro) {
        SwingUtilities.invokeLater(() -> {
            statusBanco.setForeground(new Color(0xC0392B));
            statusBanco.setText("ERRO ao gravar voto (" + voto.getCargo().getNome() + "): " + erro.getMessage());
        });
    }

    private boolean ultimoCargo(Cargo cargo) {
        var cargos = urna.getEleicao().getCargos();
        return cargos.get(cargos.size() - 1) == cargo;
    }

    // ------------------------------------------------------------------ mesário

    private void agendarProximoEleitor() {
        if (!liberarAutomatico.isSelected()) {
            tela.setSegundosParaProximo(-1);
            return;
        }
        final int[] restante = {SEGUNDOS_PROXIMO_ELEITOR};
        tela.setSegundosParaProximo(restante[0]);
        pararTimerProximo();
        timerProximo = new Timer(1000, e -> {
            restante[0]--;
            if (restante[0] <= 0) {
                liberarProximoEleitor();
            } else {
                tela.setSegundosParaProximo(restante[0]);
            }
        });
        timerProximo.start();
    }

    private void pararTimerProximo() {
        if (timerProximo != null) {
            timerProximo.stop();
            timerProximo = null;
        }
    }

    private void liberarProximoEleitor() {
        pararTimerProximo();
        tela.setSegundosParaProximo(-1);
        urna.liberarProximoEleitor();
    }

    private void abrirBoletim() {
        new TelaBoletim(this, urna.getRepositorio(), apuracao).setVisible(true);
    }

    private void zerarVotos() {
        int resp = JOptionPane.showConfirmDialog(this,
                "Apagar TODOS os votos gravados em:\n" + urna.getRepositorio().getDescricao()
                        + "\n\nUse isso apenas antes de começar uma nova votação.",
                "Zerar votos", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                urna.getRepositorio().apagarTodos();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    votosGravados.set(0);
                    atualizarStatus();
                    JOptionPane.showMessageDialog(TelaUrna.this, "Votos apagados.");
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TelaUrna.this, "Erro ao apagar: " + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void mostrarCandidatos() {
        StringBuilder sb = new StringBuilder("<html><body style='width:380px'>");
        sb.append("<p><b>Todos os partidos e candidatos são FICTÍCIOS.</b></p>");
        for (Cargo cargo : urna.getEleicao().getCargos()) {
            sb.append("<h3>").append(cargo.getNome()).append(" (")
                    .append(cargo.getQuantidadeDigitos()).append(" dígitos)</h3><ul>");
            for (Candidato c : cargo.getCandidatos()) {
                sb.append("<li><b>").append(c.getNumero()).append("</b> – ").append(c.getNome())
                        .append(" (").append(c.getPartido().getSigla()).append(")");
                if (c instanceof CandidatoMajoritario m) {
                    sb.append(" · vice: ").append(m.getVice());
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Candidatos (fictícios)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarComoVotar() {
        JOptionPane.showMessageDialog(this, """
                <html><body style='width:380px'>
                <h3>Como votar</h3>
                <ol>
                <li>Digite o número do candidato. Os dados dele aparecem na tela.</li>
                <li>Confira! Se estiver certo, aperte <b style='color:#1E9E4A'>CONFIRMA</b>.</li>
                <li>Errou? Aperte <b style='color:#E67E22'>CORRIGE</b> e digite de novo.</li>
                <li>Para votar em branco, aperte <b>BRANCO</b> (sem digitar nada) e depois CONFIRMA.</li>
                </ol>
                <p><b>Vereador</b> (5 dígitos): se o candidato não existir mas os 2 primeiros dígitos forem
                de um partido, é <b>voto de legenda</b> (vai para o partido). Se nem o partido existir, é <b>voto nulo</b>.</p>
                <p><b>Prefeito</b> (2 dígitos): número inexistente = <b>voto nulo</b>.</p>
                <h3>Teclado do computador</h3>
                <p>0–9: números · Enter: CONFIRMA · Backspace: CORRIGE · B: BRANCO</p>
                </body></html>
                """, "Como votar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this, """
                <html><body style='width:360px'>
                <h3>Urna Cidadã</h3>
                <p>Simulador educativo de urna eletrônica.</p>
                <p>Programação Orientada a Objetos<br>
                Ciência da Computação – UNIJORGE – 2026</p>
                <p>Java Swing + Firebase (Cloud Firestore).</p>
                <p><i>Ferramenta didática e apartidária. Não é a urna oficial e não tem
                vínculo com a Justiça Eleitoral. Candidatos e partidos são fictícios.</i></p>
                </body></html>
                """, "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------ menu, atalhos e status

    private JMenuBar criarMenu() {
        JMenu mesario = new JMenu("Mesário");
        mesario.add(item("Liberar próximo eleitor", KeyEvent.VK_L, e -> liberarProximoEleitor()));
        mesario.add(item("Boletim de urna (apuração)", KeyEvent.VK_R, e -> abrirBoletim()));
        mesario.addSeparator();
        mesario.add(liberarAutomatico);
        JCheckBoxMenuItem somAtivo = new JCheckBoxMenuItem("Som das teclas", true);
        somAtivo.addActionListener(e -> som.setAtivo(somAtivo.isSelected()));
        mesario.add(somAtivo);
        mesario.addSeparator();
        mesario.add(item("Zerar votos...", 0, e -> zerarVotos()));
        mesario.addSeparator();
        mesario.add(item("Sair", KeyEvent.VK_Q, e -> dispose()));

        JMenu ajuda = new JMenu("Ajuda");
        ajuda.add(item("Como votar", KeyEvent.VK_H, e -> mostrarComoVotar()));
        ajuda.add(item("Lista de candidatos (fictícios)", 0, e -> mostrarCandidatos()));
        ajuda.add(item("Sobre", 0, e -> mostrarSobre()));

        JMenuBar barra = new JMenuBar();
        barra.add(mesario);
        barra.add(ajuda);
        return barra;
    }

    private static JMenuItem item(String texto, int teclaCtrl, java.awt.event.ActionListener acao) {
        JMenuItem i = new JMenuItem(texto);
        if (teclaCtrl != 0) {
            i.setAccelerator(KeyStroke.getKeyStroke(teclaCtrl, InputEvent.CTRL_DOWN_MASK));
        }
        i.addActionListener(acao);
        return i;
    }

    /** Permite votar também pelo teclado do computador. */
    private void configurarAtalhos() {
        JComponent raiz = getRootPane();
        for (int i = 0; i <= 9; i++) {
            final int n = i;
            AbstractAction a = acao(() -> teclado.pressionarDigito(n));
            vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0), "digito" + i, a);
            vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, 0), "digito" + i, a);
        }
        vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirma", acao(teclado::pressionarConfirma));
        vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "corrige", acao(teclado::pressionarCorrige));
        vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "corrige", acao(teclado::pressionarCorrige));
        vincular(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "branco", acao(teclado::pressionarBranco));
    }

    private static void vincular(JComponent c, KeyStroke tecla, String nome, AbstractAction acao) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nome);
        c.getActionMap().put(nome, acao);
    }

    private static AbstractAction acao(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
    }

    private void atualizarStatus() {
        boolean offline = urna.getRepositorio() instanceof MemoriaVotoRepository;
        statusBanco.setForeground(offline ? new Color(0xB9770E) : new Color(0x1E8449));
        statusBanco.setText("● " + urna.getRepositorio().getDescricao());
        statusContagem.setText("Eleitores que votaram: " + urna.getEleitoresQueVotaram()
                + "   |   Votos gravados: " + votosGravados.get());
    }
}
