package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.Eleicao;
import br.edu.unijorge.urnacidada.model.Partido;
import br.edu.unijorge.urnacidada.model.RegistroVoto;
import br.edu.unijorge.urnacidada.model.TipoVoto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Faz a contagem dos votos gravados e monta o boletim de urna. */
public class ApuracaoService {

    private final Eleicao eleicao;

    public ApuracaoService(Eleicao eleicao) {
        this.eleicao = Objects.requireNonNull(eleicao);
    }

    /** Apura os votos recebidos, devolvendo um resultado por cargo (na ordem da eleição). */
    public List<ResultadoCargo> apurar(List<RegistroVoto> votos) {
        List<ResultadoCargo> resultados = new ArrayList<>();
        for (Cargo cargo : eleicao.getCargos()) {
            resultados.add(apurarCargo(cargo, votos));
        }
        return resultados;
    }

    private ResultadoCargo apurarCargo(Cargo cargo, List<RegistroVoto> todos) {
        List<RegistroVoto> votos = todos.stream()
                .filter(v -> cargo.getNome().equals(v.cargo()))
                .toList();

        // candidatos cadastrados começam com zero votos, para aparecerem no boletim
        Map<String, long[]> nominais = new LinkedHashMap<>();
        Map<String, ResultadoCargo.Linha> dadosCandidato = new LinkedHashMap<>();
        for (Candidato c : cargo.getCandidatos()) {
            nominais.put(c.getNumero(), new long[]{0});
            dadosCandidato.put(c.getNumero(),
                    new ResultadoCargo.Linha(c.getNumero(), c.getNome(), c.getPartido().getSigla(), 0));
        }

        Map<String, long[]> porPartido = new LinkedHashMap<>();
        for (Partido p : cargo.getPartidos()) {
            porPartido.put(p.getSigla(), new long[]{0});
        }

        long legenda = 0;
        long brancos = 0;
        long nulos = 0;
        for (RegistroVoto v : votos) {
            switch (v.tipo()) {
                case NOMINAL -> {
                    nominais.computeIfAbsent(v.numero(), n -> new long[]{0})[0]++;
                    dadosCandidato.putIfAbsent(v.numero(),
                            new ResultadoCargo.Linha(v.numero(), v.descricao(), v.siglaPartido(), 0));
                    somarPartido(porPartido, v.siglaPartido());
                }
                case LEGENDA -> {
                    legenda++;
                    somarPartido(porPartido, v.siglaPartido());
                }
                case BRANCO -> brancos++;
                case NULO -> nulos++;
            }
        }

        List<ResultadoCargo.Linha> linhasCandidatos = new ArrayList<>();
        nominais.forEach((numero, qtd) -> {
            ResultadoCargo.Linha base = dadosCandidato.get(numero);
            linhasCandidatos.add(new ResultadoCargo.Linha(numero, base.nome(), base.siglaPartido(), qtd[0]));
        });
        linhasCandidatos.sort(Comparator.comparingLong(ResultadoCargo.Linha::votos).reversed()
                .thenComparing(ResultadoCargo.Linha::numero));

        List<ResultadoCargo.Linha> linhasPartidos = new ArrayList<>();
        porPartido.forEach((sigla, qtd) -> {
            String numero = cargo.getPartidos().stream()
                    .filter(p -> p.getSigla().equals(sigla))
                    .map(p -> String.valueOf(p.getNumero()))
                    .findFirst().orElse("");
            String nome = cargo.getPartidos().stream()
                    .filter(p -> p.getSigla().equals(sigla))
                    .map(Partido::getNome)
                    .findFirst().orElse(sigla);
            linhasPartidos.add(new ResultadoCargo.Linha(numero, nome, sigla, qtd[0]));
        });
        linhasPartidos.sort(Comparator.comparingLong(ResultadoCargo.Linha::votos).reversed()
                .thenComparing(ResultadoCargo.Linha::numero));

        return new ResultadoCargo(cargo.getNome(), cargo.getTipo(), linhasCandidatos, linhasPartidos,
                legenda, brancos, nulos);
    }

    private static void somarPartido(Map<String, long[]> porPartido, String sigla) {
        if (sigla != null) {
            porPartido.computeIfAbsent(sigla, s -> new long[]{0})[0]++;
        }
    }

    /** Boletim em texto puro (para imprimir no console ou copiar para o relatório). */
    public String gerarBoletimTexto(List<ResultadoCargo> resultados) {
        StringBuilder sb = new StringBuilder();
        sb.append("BOLETIM DE URNA – ").append(eleicao.getNome()).append('\n');
        sb.append("=".repeat(60)).append('\n');
        for (ResultadoCargo r : resultados) {
            sb.append('\n').append(r.cargo().toUpperCase()).append(" (").append(r.tipoCargo()).append(")\n");
            sb.append("-".repeat(60)).append('\n');
            for (ResultadoCargo.Linha l : r.candidatos()) {
                sb.append(String.format("%-6s %-22s %-6s %5d  %6.2f%%%n",
                        l.numero(), l.nome(), l.siglaPartido(), l.votos(), r.percentualValidos(l.votos())));
            }
            if (r.isProporcional()) {
                sb.append(String.format("%-36s %5d%n", "Votos de legenda", r.legenda()));
            }
            sb.append(String.format("%-36s %5d%n", "Votos válidos", r.validos()));
            sb.append(String.format("%-36s %5d%n", "Brancos", r.brancos()));
            sb.append(String.format("%-36s %5d%n", "Nulos", r.nulos()));
            sb.append(String.format("%-36s %5d%n", "Total", r.total()));
        }
        return sb.toString();
    }

    /** Atalho usado na contagem de um tipo específico em testes/relatórios. */
    public static long contar(List<RegistroVoto> votos, String cargo, TipoVoto tipo) {
        return votos.stream().filter(v -> v.cargo().equals(cargo) && v.tipo() == tipo).count();
    }
}
