package br.com.techstore.repository;

import br.com.techstore.model.Pedido;

/**
 * Acesso a dados de pedidos. {@code salvar} é {@code void}: para simular falhas
 * nos testes usa-se {@code doThrow(...).when(repo).salvar(...)}.
 */
public interface PedidoRepository {

    void salvar(Pedido pedido);
}
