package br.com.techstore.service;

import br.com.techstore.exception.ErroAoSalvarProdutoException;
import br.com.techstore.exception.ProdutoNaoEncontradoException;
import br.com.techstore.model.Produto;
import br.com.techstore.repository.ProdutoRepository;

/**
 * Regras de negócio do cadastro de produtos (RF01 a RF04).
 */
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto salvar(Produto produto) {
        validar(produto);
        try {
            return repository.salvar(produto);
        } catch (RuntimeException e) {
            throw new ErroAoSalvarProdutoException("Falha ao salvar o produto", e);
        }
    }

    public Produto buscarPorId(Long id) {
        Produto produto = repository.buscarPorId(id);
        if (produto == null) {
            throw new ProdutoNaoEncontradoException("Produto não encontrado: " + id);
        }
        return produto;
    }

    private void validar(Produto produto) {
        if (produto == null || produto.getNome() == null
                || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        if (produto.getPreco() <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        if (produto.getQuantidade() < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
    }
}
