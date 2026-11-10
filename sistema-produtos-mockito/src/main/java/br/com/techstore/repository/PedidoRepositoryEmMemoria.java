package br.com.techstore.repository;

import br.com.techstore.model.Pedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação de PedidoRepository que guarda os pedidos na MEMÓRIA
 * (sem banco de dados). Usada pelo programa de console {@code App}.
 */
public class PedidoRepositoryEmMemoria implements PedidoRepository {

    private final List<Pedido> pedidos = new ArrayList<>();

    @Override
    public void salvar(Pedido pedido) {
        pedidos.add(pedido);
    }

    /** Todos os pedidos criados, na ordem de criação. */
    public List<Pedido> listarTodos() {
        return Collections.unmodifiableList(pedidos);
    }
}
