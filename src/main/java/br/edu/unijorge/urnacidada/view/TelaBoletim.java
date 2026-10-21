package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.RegistroVoto;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import br.edu.unijorge.urnacidada.service.ApuracaoService;
import br.edu.unijorge.urnacidada.service.ResultadoCargo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Boletim de urna: lê os votos do banco e mostra a apuração por cargo.
 * A leitura roda em segundo plano (SwingWorker) para não travar a janela.
 */
public class TelaBoletim extends JDialog {

    private final VotoRepository repositorio;
    private final ApuracaoService apuracao;
    private final JTabbedPane abas = new JTabbedPane();
    private final JLabel status = new JLabel(" ");
    private final JButton botaoAtualizar = new JButton("Atualizar");
    private String textoBoletim = "";

    public TelaBoletim(JFrame dono, VotoRepository repositorio, ApuracaoService apuracao) {
        super(dono, "Boletim de urna (apuração)", true);
        this.repositorio = repositorio;
        this.apuracao = apuracao;

        JButton copiar = new JButton("Copiar boletim em texto");
        copiar.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textoBoletim), null);
            status.setText("Boletim copiado para a área de transferência.");
        });
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());
        botaoAtualizar.addActionListener(e -> carregar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(botaoAtualizar);
        botoes.add(copiar);
        botoes.add(fechar);

        JPanel sul = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        sul.add(status, BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(abas, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);
        setSize(new Dimension(760, 560));
        setLocationRelativeTo(dono);

        carregar();
    }

    private void carregar() {
        botaoAtualizar.setEnabled(false);
        status.setText("Lendo votos em: " + repositorio.getDescricao() + " ...");
        new SwingWorker<List<ResultadoCargo>, Void>() {
            @Override
            protected List<ResultadoCargo> doInBackground() {
                List<RegistroVoto> votos = repositorio.listarTodos();
                return apuracao.apurar(votos);
            }

            @Override
            protected void done() {
                botaoAtualizar.setEnabled(true);
                try {
                    mostrar(get());
                    status.setText("Apuração concluída.");
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    status.setText("Erro ao ler os votos: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private void mostrar(List<ResultadoCargo> resultados) {
        abas.removeAll();
        for (ResultadoCargo r : resultados) {
            abas.addTab(r.cargo(), criarAbaCargo(r));
        }
        textoBoletim = apuracao.gerarBoletimTexto(resultados);
        JTextArea texto = new JTextArea(textoBoletim);
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        abas.addTab("Boletim em texto", new JScrollPane(texto));
    }

    private JPanel criarAbaCargo(ResultadoCargo r) {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel modelo = modeloSomenteLeitura("Número", "Candidato", "Partido", "Votos", "% válidos");
        for (ResultadoCargo.Linha l : r.candidatos()) {
            modelo.addRow(new Object[]{l.numero(), l.nome(), l.siglaPartido(), l.votos(),
                    String.format("%.2f%%", r.percentualValidos(l.votos()))});
        }
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(rotulo("Votos nominais por candidato"));
        centro.add(new JScrollPane(tabela(modelo)));

        if (r.isProporcional()) {
            DefaultTableModel partidos = modeloSomenteLeitura("Número", "Partido", "Sigla", "Votos (nominais + legenda)");
            for (ResultadoCargo.Linha l : r.partidos()) {
                partidos.addRow(new Object[]{l.numero(), l.nome(), l.siglaPartido(), l.votos()});
            }
            centro.add(rotulo("Votos por partido"));
            centro.add(new JScrollPane(tabela(partidos)));
        }
        painel.add(centro, BorderLayout.CENTER);

        JPanel resumo = new JPanel(new GridLayout(1, 0, 10, 0));
        resumo.add(caixaNumero("Válidos", r.validos()));
        if (r.isProporcional()) {
            resumo.add(caixaNumero("Legenda", r.legenda()));
        }
        resumo.add(caixaNumero("Brancos", r.brancos()));
        resumo.add(caixaNumero("Nulos", r.nulos()));
        resumo.add(caixaNumero("Total", r.total()));
        painel.add(resumo, BorderLayout.SOUTH);
        return painel;
    }

    private static DefaultTableModel modeloSomenteLeitura(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable tabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        t.setPreferredScrollableViewportSize(new Dimension(700, Math.min(8, modelo.getRowCount()) * 24 + 4));
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 3; i < modelo.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(direita);
        }
        return t;
    }

    private static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel caixaNumero(String titulo, long valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        JLabel n = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        n.setFont(n.getFont().deriveFont(Font.BOLD, 22f));
        p.add(n);
        return p;
    }
}
