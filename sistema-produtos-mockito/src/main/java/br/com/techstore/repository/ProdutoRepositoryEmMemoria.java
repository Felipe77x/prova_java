package br.com.techstore.repository;

import br.com.techstore.model.Produto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementação de ProdutoRepository que guarda os produtos na MEMÓRIA
 * (sem banco de dados). Usada pelo programa de console {@code App}.
 * Os dados somem quando o programa termina.
 */
public class ProdutoRepositoryEmMemoria implements ProdutoRepository {

    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Produto salvar(Produto produto) {
        produtos.put(proximoId, produto);
        proximoId++;
        return produto;
    }

    @Override
    public Produto buscarPorId(Long id) {
        return produtos.get(id);
    }

    /** Todos os produtos cadastrados, na ordem de cadastro (ID -> produto). */
    public Map<Long, Produto> listarTodos() {
        return Collections.unmodifiableMap(produtos);
    }

    /** ID gerado no último {@link #salvar(Produto)} (0 se nada foi salvo). */
    public long ultimoIdGerado() {
        return proximoId - 1;
    }
}
