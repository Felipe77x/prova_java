package br.com.techstore.exception;

public class ErroAoSalvarProdutoException extends RuntimeException {

    public ErroAoSalvarProdutoException(String mensagem) {
        super(mensagem);
    }

    public ErroAoSalvarProdutoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
