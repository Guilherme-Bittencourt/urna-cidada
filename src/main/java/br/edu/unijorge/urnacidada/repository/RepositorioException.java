package br.edu.unijorge.urnacidada.repository;

/** Erro ao acessar o banco de dados. */
public class RepositorioException extends RuntimeException {

    public RepositorioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
