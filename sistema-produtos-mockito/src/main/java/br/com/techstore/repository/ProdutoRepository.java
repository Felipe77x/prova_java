package br.com.techstore.repository;

import br.com.techstore.model.Produto;

/**
 * Acesso a dados de produtos. É uma interface, por isso pode ser substituída
 * por um mock do Mockito nos testes unitários do Service.
 */
public interface ProdutoRepository {

    Produto salvar(Produto produto);

    /** Retorna o produto ou {@code null} quando não existir. */
    Produto buscarPorId(Long id);
}
