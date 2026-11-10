package br.com.techstore.service;

import br.com.techstore.exception.ErroAoSalvarPedidoException;
import br.com.techstore.exception.EstoqueInsuficienteException;
import br.com.techstore.exception.ProdutoNaoEncontradoException;
import br.com.techstore.model.Pedido;
import br.com.techstore.model.Produto;
import br.com.techstore.repository.PedidoRepository;
import br.com.techstore.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do {@link PedidoService} (Atividades 6 e 7).
 *
 * Dois @Mock (ProdutoRepository e PedidoRepository) e um @InjectMocks
 * (PedidoService real). Nenhum teste acessa banco de dados.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService service;

    // ================================================================
    // Caminho feliz (PED01, PED02, PED07)
    // ================================================================

    @Test
    @DisplayName("PED01 - Deve calcular o valor total: 49,90 x 2 = 99,80")
    void deveCalcularValorTotalDoPedido() {
        // Arrange
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        // Act
        Pedido pedido = service.criarPedido(1L, 2);

        // Assert
        assertNotNull(pedido);
        assertEquals(99.80, pedido.getValorTotal(), 0.001);
        assertEquals(2, pedido.getQuantidade());
        assertSame(produto, pedido.getProduto());
    }

    @Test
    @DisplayName("PED01 - Deve salvar no repositório o mesmo pedido calculado (ArgumentCaptor)")
    void deveSalvarOPedidoCalculadoNoRepositorio() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

        Pedido pedido = service.criarPedido(1L, 2);

        verify(pedidoRepository).salvar(captor.capture());
        Pedido salvo = captor.getValue();
        assertSame(pedido, salvo);
        assertEquals(99.80, salvo.getValorTotal(), 0.001);
        assertEquals(2, salvo.getQuantidade());
    }

    @Test
    @DisplayName("PED02 - Deve chamar cada repositório exatamente uma vez")
    void deveConsultarProdutoESalvarPedidoUmaVezCada() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        service.criarPedido(1L, 2);

        verify(produtoRepository, times(1)).buscarPorId(1L);
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
        verifyNoMoreInteractions(produtoRepository, pedidoRepository);
    }

    @Test
    @DisplayName("PED02 - Deve buscar o produto ANTES de salvar o pedido (InOrder)")
    void deveBuscarProdutoAntesDeSalvarPedido() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        service.criarPedido(1L, 2);

        InOrder ordem = inOrder(produtoRepository, pedidoRepository);
        ordem.verify(produtoRepository).buscarPorId(1L);
        ordem.verify(pedidoRepository).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("PED07 - Deve aceitar quantidade igual ao estoque (10 com estoque 10)")
    void deveAceitarPedidoComQuantidadeIgualAoEstoque() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        Pedido pedido = service.criarPedido(1L, 10);

        assertNotNull(pedido);
        assertEquals(10, pedido.getQuantidade());
        assertEquals(499.00, pedido.getValorTotal(), 0.001);
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }

    // ================================================================
    // Erros: o pedido NÃO pode ser salvo (PED03 a PED06 e PED08)
    // ================================================================

    @Test
    @DisplayName("PED03 - Não deve criar pedido com quantidade igual a zero")
    void naoDeveCriarPedidoComQuantidadeIgualAZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(1L, 0));

        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("PED04 - Não deve criar pedido com quantidade negativa (-3)")
    void naoDeveCriarPedidoComQuantidadeNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(1L, -3));

        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("PED05 - Deve lançar ProdutoNaoEncontradoException quando o produto não existe (ID 99)")
    void deveLancarExcecaoQuandoProdutoDoPedidoNaoExiste() {
        when(produtoRepository.buscarPorId(99L)).thenReturn(null);

        ProdutoNaoEncontradoException ex = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> service.criarPedido(99L, 1));

        assertEquals("Produto não encontrado: 99", ex.getMessage());
        verify(produtoRepository).buscarPorId(99L);
        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("PED06 - Deve lançar EstoqueInsuficienteException (11 com estoque 10)")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        EstoqueInsuficienteException ex = assertThrows(
                EstoqueInsuficienteException.class,
                () -> service.criarPedido(1L, 11));

        assertEquals("Estoque insuficiente", ex.getMessage());
        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("PED08 - O pedido não é salvo em nenhum dos cenários de erro (PED03 a PED06)")
    void naoDeveSalvarPedidoEmNenhumCenarioDeErro() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        when(produtoRepository.buscarPorId(99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criarPedido(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> service.criarPedido(1L, -3));
        assertThrows(ProdutoNaoEncontradoException.class, () -> service.criarPedido(99L, 1));
        assertThrows(EstoqueInsuficienteException.class, () -> service.criarPedido(1L, 11));

        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }

    @ParameterizedTest(name = "quantidade {0} -> IllegalArgumentException, sem consultar nem salvar")
    @ValueSource(ints = {0, -1, -3, -100, Integer.MIN_VALUE})
    @DisplayName("Extra - Quantidade inválida não consulta produto nem salva pedido")
    void naoDeveConsultarNemSalvarComQuantidadeInvalida(int quantidade) {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(1L, quantidade));

        verifyNoInteractions(produtoRepository, pedidoRepository);
    }

    // ================================================================
    // ATIVIDADE 7 - Simulando falhas com doThrow
    // ================================================================

    // Cenário A + D (desafio): o PedidoService agora captura a falha do
    // PedidoRepository e lança ErroAoSalvarPedidoException.
    @Test
    @DisplayName("Atividade 7 / Cenários A e D - Falha do PedidoRepository vira ErroAoSalvarPedidoException")
    void deveLancarErroAoSalvarPedidoQuandoRepositoryFalha() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        doThrow(new RuntimeException("Falha na conexão"))
                .when(pedidoRepository).salvar(any(Pedido.class));

        ErroAoSalvarPedidoException ex = assertThrows(
                ErroAoSalvarPedidoException.class,
                () -> service.criarPedido(1L, 2));

        assertEquals("Falha ao salvar o pedido", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("Falha na conexão", ex.getCause().getMessage());
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }

    // Cenário B - when(...).thenReturn(...) em método void NÃO compila:
    //
    //     when(pedidoRepository.salvar(any())).thenReturn(...);   // ERRO
    //
    // salvar() é void: a expressão não tem valor e não pode ser passada a
    // when(T). Forma correta: doThrow(...).when(repo).salvar(...) ou
    // doNothing().when(repo).salvar(...).
    @Test
    @DisplayName("Atividade 7 / Cenário B - Forma correta de programar método void: doNothing()")
    void devePermitirDoNothingEmMetodoVoid() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        doNothing().when(pedidoRepository).salvar(any(Pedido.class));

        Pedido pedido = service.criarPedido(1L, 2);

        assertNotNull(pedido);
        verify(pedidoRepository).salvar(pedido);
    }

    @Test
    @DisplayName("Atividade 7 / Cenário C - Falha no ProdutoRepository: o pedido não é salvo")
    void naoDeveSalvarPedidoQuandoProdutoRepositoryFalha() {
        when(produtoRepository.buscarPorId(1L))
                .thenThrow(new RuntimeException("Banco de produtos fora do ar"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.criarPedido(1L, 2));

        assertEquals("Banco de produtos fora do ar", ex.getMessage());
        verify(pedidoRepository, never()).salvar(any(Pedido.class));
    }
}
