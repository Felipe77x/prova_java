package br.com.techstore.exception;

/**
 * Desafio (Atividade 7, Cenário D): lançada quando o PedidoRepository falha
 * ao salvar o pedido.
 */
public class ErroAoSalvarPedidoException extends RuntimeException {

    public ErroAoSalvarPedidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
