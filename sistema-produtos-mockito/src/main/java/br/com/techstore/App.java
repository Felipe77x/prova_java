package br.com.techstore;

import br.com.techstore.exception.ErroAoSalvarPedidoException;
import br.com.techstore.exception.ErroAoSalvarProdutoException;
import br.com.techstore.exception.EstoqueInsuficienteException;
import br.com.techstore.exception.ProdutoNaoEncontradoException;
import br.com.techstore.model.Pedido;
import br.com.techstore.model.Produto;
import br.com.techstore.repository.PedidoRepositoryEmMemoria;
import br.com.techstore.repository.ProdutoRepositoryEmMemoria;
import br.com.techstore.service.PedidoService;
import br.com.techstore.service.ProdutoService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Programa de console da TechStore Distribuidora.
 *
 * Permite cadastrar produtos à mão, buscar, listar e criar pedidos usando os
 * Services reais (ProdutoService e PedidoService) com repositórios em memória.
 * Os dados ficam apenas na memória e somem quando o programa termina.
 */
public class App {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private final Scanner entrada;
    private final ProdutoRepositoryEmMemoria produtoRepository = new ProdutoRepositoryEmMemoria();
    private final PedidoRepositoryEmMemoria pedidoRepository = new PedidoRepositoryEmMemoria();
    private final ProdutoService produtoService = new ProdutoService(produtoRepository);
    private final PedidoService pedidoService = new PedidoService(produtoRepository, pedidoRepository);

    public App(Scanner entrada) {
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new App(scanner).executar();
        }
    }

    public void executar() {
        System.out.println("=== TechStore Distribuidora ===");
        try {
            boolean continuar = true;
            while (continuar) {
                mostrarMenu();
                String opcao = lerTexto("Escolha uma opção: ");
                switch (opcao) {
                    case "1" -> cadastrarProduto();
                    case "2" -> listarProdutos();
                    case "3" -> buscarProduto();
                    case "4" -> criarPedido();
                    case "5" -> listarPedidos();
                    case "0" -> continuar = false;
                    default -> System.out.println("Opção inválida. Digite um número do menu.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println();   // fim da entrada (Ctrl+D / Ctrl+Z)
        }
        System.out.println("Até logo!");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Listar produtos");
        System.out.println("3 - Buscar produto por ID");
        System.out.println("4 - Criar pedido");
        System.out.println("5 - Listar pedidos");
        System.out.println("0 - Sair");
    }

    // ---------------------------------------------------------------
    // Opções do menu
    // ---------------------------------------------------------------

    private void cadastrarProduto() {
        System.out.println("-- Cadastro de produto --");
        String nome = lerTexto("Nome: ");
        double preco = lerDecimal("Preço (ex.: 49,90): ");
        int quantidade = lerInteiro("Quantidade em estoque: ");
        try {
            produtoService.salvar(new Produto(nome, preco, quantidade));
            System.out.println("Produto cadastrado com sucesso! ID: " + produtoRepository.ultimoIdGerado());
        } catch (IllegalArgumentException | ErroAoSalvarProdutoException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    private void listarProdutos() {
        System.out.println("-- Produtos cadastrados --");
        Map<Long, Produto> produtos = produtoRepository.listarTodos();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        produtos.forEach((id, produto) -> System.out.println(descreverProduto(id, produto)));
    }

    private void buscarProduto() {
        Long id = lerInteiro("ID do produto: ") * 1L;
        try {
            Produto produto = produtoService.buscarPorId(id);
            System.out.println(descreverProduto(id, produto));
        } catch (ProdutoNaoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }

    private void criarPedido() {
        System.out.println("-- Novo pedido --");
        Long id = lerInteiro("ID do produto: ") * 1L;
        int quantidade = lerInteiro("Quantidade: ");
        try {
            Pedido pedido = pedidoService.criarPedido(id, quantidade);
            System.out.println("Pedido criado! " + descreverPedido(pedido));
        } catch (IllegalArgumentException | ProdutoNaoEncontradoException
                 | EstoqueInsuficienteException | ErroAoSalvarPedidoException e) {
            System.out.println("Não foi possível criar o pedido: " + e.getMessage());
        }
    }

    private void listarPedidos() {
        System.out.println("-- Pedidos criados --");
        List<Pedido> pedidos = pedidoRepository.listarTodos();
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido criado.");
            return;
        }
        int numero = 1;
        for (Pedido pedido : pedidos) {
            System.out.println("#" + numero++ + " " + descreverPedido(pedido));
        }
    }

    // ---------------------------------------------------------------
    // Formatação
    // ---------------------------------------------------------------

    private String descreverProduto(Long id, Produto produto) {
        return String.format(BR, "ID %d | %s | R$ %.2f | estoque: %d",
                id, produto.getNome(), produto.getPreco(), produto.getQuantidade());
    }

    private String descreverPedido(Pedido pedido) {
        return String.format(BR, "%s x %d = R$ %.2f",
                pedido.getProduto().getNome(), pedido.getQuantidade(), pedido.getValorTotal());
    }

    // ---------------------------------------------------------------
    // Leitura de dados (repete até o usuário digitar um valor válido)
    // ---------------------------------------------------------------

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return entrada.nextLine().trim();
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private double lerDecimal(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número (ex.: 49,90).");
            }
        }
    }
}
