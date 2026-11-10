package br.com.techstore.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
