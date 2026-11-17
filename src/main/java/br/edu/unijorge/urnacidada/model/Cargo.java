package br.edu.unijorge.urnacidada.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Cargo em disputa (ABSTRAÇÃO). Cada subclasse define quantos dígitos tem
 * o número e como o número digitado vira um voto (método interpretar = POLIMORFISMO).
 */
public abstract class Cargo {

    private final String nome;
    private final Map<String, Candidato> candidatos = new HashMap<>();

    public Cargo(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }

    public abstract int getQuantidadeDigitos();

    public abstract Voto interpretar(String numero);

    public void adicionarCandidato(Candidato candidato) {
        candidatos.put(candidato.getNumero(), candidato);
    }

    /** Retorna o candidato com esse número, ou null se não existir. */
    public Candidato buscarCandidato(String numero) {
        return candidatos.get(numero);
    }

    /** Procura o partido pelos 2 primeiros dígitos. Retorna null se não existir. */
    public Partido buscarPartido(String numero) {
        if (numero.length() < 2) {
            return null;
        }
        int numeroPartido = Integer.parseInt(numero.substring(0, 2));
        for (Candidato c : candidatos.values()) {
            if (c.getPartido().getNumero() == numeroPartido) {
                return c.getPartido();
            }
        }
        return null;
    }
}
