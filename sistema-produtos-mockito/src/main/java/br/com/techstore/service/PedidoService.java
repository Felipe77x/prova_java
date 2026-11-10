package br.com.techstore.service;

import br.com.techstore.exception.ErroAoSalvarPedidoException;
import br.com.techstore.exception.EstoqueInsuficienteException;
import br.com.techstore.exception.ProdutoNaoEncontradoException;
import br.com.techstore.model.Pedido;
import br.com.techstore.model.Produto;
import br.com.techstore.repository.PedidoRepository;
import br.com.techstore.repository.ProdutoRepository;

/**
 * Regras de negócio da criação de pedidos (RF05).
 * Depende de dois repositórios: ProdutoRepository (consulta) e PedidoRepository (grava).
 */
public class PedidoService {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public PedidoService(ProdutoRepository produtoRepository,
                         PedidoRepository pedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Cria e salva um pedido.
     *
     * @throws IllegalArgumentException      se a quantidade for menor ou igual a zero
     * @throws ProdutoNaoEncontradoException se o produto não existir
     * @throws EstoqueInsuficienteException  se a quantidade passar do estoque
     * @throws ErroAoSalvarPedidoException   se o PedidoRepository falhar ao salvar
     */
    public Pedido criarPedido(Long produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade deve ser maior que zero");
        }
        Produto produto = produtoRepository.buscarPorId(produtoId);
        if (produto == null) {
            throw new ProdutoNaoEncontradoException(
                    "Produto não encontrado: " + produtoId);
        }
        if (quantidade > produto.getQuantidade()) {
            throw new EstoqueInsuficienteException("Estoque insuficiente");
        }
        double total = produto.getPreco() * quantidade;
        Pedido pedido = new Pedido(produto, quantidade, total);
        try {
            pedidoRepository.salvar(pedido);
        } catch (RuntimeException e) {
            throw new ErroAoSalvarPedidoException("Falha ao salvar o pedido", e);
        }
        return pedido;
    }
}
