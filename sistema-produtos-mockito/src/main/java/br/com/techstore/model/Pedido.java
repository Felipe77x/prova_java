package br.com.techstore.model;

/**
 * Pedido de um produto, com a quantidade e o valor total já calculados.
 */
public class Pedido {

    private final Produto produto;
    private final int quantidade;
    private final double valorTotal;

    public Pedido(Produto produto, int quantidade, double valorTotal) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getValorTotal() { return valorTotal; }

    @Override
    public String toString() {
        return "Pedido{produto=" + produto + ", quantidade=" + quantidade
                + ", valorTotal=" + valorTotal + "}";
    }
}
