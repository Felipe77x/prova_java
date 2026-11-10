package br.com.techstore.service;

import br.com.techstore.exception.ErroAoSalvarProdutoException;
import br.com.techstore.exception.ProdutoNaoEncontradoException;
import br.com.techstore.model.Produto;
import br.com.techstore.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do {@link ProdutoService} (Atividades 3, 4 e 5).
 *
 * <ul>
 *   <li>{@code ProdutoRepository} é MOCK (@Mock): nunca acessa banco de dados.</li>
 *   <li>{@code ProdutoService} é a classe REAL testada (@InjectMocks).</li>
 * </ul>
 *
 * Os testes CT01 a CT10 seguem o plano da Atividade 2; CT11 a CT16 são os
 * cenários do analista (Atividade 3). Ao final há testes extras parametrizados.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    // ================================================================
    // RF01 - Cadastro de produto
    // ================================================================

    @Test
    @DisplayName("CT01 - Deve salvar produto válido e devolver o produto salvo")
    void deveSalvarProdutoComPrecoValido() {
        // Arrange
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(repository.salvar(produto)).thenReturn(produto);

        // Act
        Produto resultado = service.salvar(produto);

        // Assert
        assertEquals("Mouse", resultado.getNome());
        assertEquals(49.90, resultado.getPreco(), 0.001);
        assertEquals(10, resultado.getQuantidade());
        verify(repository).salvar(produto);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("CT16 - Deve salvar usando matcher any(Produto.class) no mock")
    void deveSalvarProdutoUsandoAnyNoMock() {
        Produto produto = new Produto("Monitor", 899.00, 3);
        when(repository.salvar(any(Produto.class))).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertSame(produto, resultado);
        verify(repository, times(1)).salvar(any(Produto.class));
    }

    // ================================================================
    // RF02 - Validação dos dados (o Repository NUNCA deve ser chamado)
    // ================================================================

    @Test
    @DisplayName("CT02 - Não deve salvar produto com preço negativo")
    void naoDeveSalvarProdutoComPrecoNegativo() {
        Produto produto = new Produto("Mouse", -10, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("CT03/CT11 - Não deve salvar produto com preço igual a zero")
    void naoDeveSalvarProdutoComPrecoIgualAZero() {
        Produto produto = new Produto("Mouse", 0, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        assertEquals("Preço deve ser maior que zero", ex.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("CT04 - Não deve salvar produto com nome vazio")
    void naoDeveSalvarProdutoComNomeVazio() {
        Produto produto = new Produto("", 49.90, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        assertEquals("Nome do produto é obrigatório", ex.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("CT05 - Não deve salvar produto com nome nulo")
    void naoDeveSalvarProdutoComNomeNulo() {
        Produto produto = new Produto(null, 49.90, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        assertEquals("Nome do produto é obrigatório", ex.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("CT06 - Não deve salvar produto com quantidade negativa")
    void naoDeveSalvarProdutoComQuantidadeNegativa() {
        Produto produto = new Produto("Mouse", 49.90, -1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        assertEquals("Quantidade não pode ser negativa", ex.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("CT07 - Deve aceitar quantidade igual a zero (estoque zerado é permitido)")
    void deveSalvarProdutoComQuantidadeIgualAZero() {
        Produto produto = new Produto("Mouse", 49.90, 0);
        when(repository.salvar(produto)).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertSame(produto, resultado);
        assertEquals(0, resultado.getQuantidade());
        verify(repository).salvar(produto);
    }

    @Test
    @DisplayName("CT12 - Deve aceitar preço 0.01 (menor valor válido, fronteira)")
    void deveSalvarProdutoComPrecoMinimoValido() {
        Produto produto = new Produto("Adesivo", 0.01, 100);
        when(repository.salvar(produto)).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertEquals(0.01, resultado.getPreco(), 0.0001);
        verify(repository).salvar(produto);
    }

    @Test
    @DisplayName("CT13 - Não deve salvar produto com nome em branco (\"   \")")
    void naoDeveSalvarProdutoComNomeEmBranco() {
        Produto produto = new Produto("   ", 49.90, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Extra - Não deve salvar produto nulo")
    void naoDeveSalvarProdutoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(null));

        verifyNoInteractions(repository);
    }

    // ================================================================
    // RF03 - Busca por ID
    // ================================================================

    @Test
    @DisplayName("CT08 - Deve retornar o produto quando ele existir (ID 1)")
    void deveRetornarProdutoQuandoExistir() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(repository.buscarPorId(1L)).thenReturn(produto);

        Produto resultado = service.buscarPorId(1L);

        assertSame(produto, resultado);
        verify(repository).buscarPorId(1L);
    }

    @Test
    @DisplayName("CT09/CT14 - Deve lançar ProdutoNaoEncontradoException quando o Repository devolve null (ID 99)")
    void deveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.buscarPorId(99L)).thenReturn(null);

        ProdutoNaoEncontradoException ex = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Produto não encontrado: 99", ex.getMessage());
        verify(repository).buscarPorId(99L);
    }

    // ================================================================
    // RF04 - Falha no Repository (doThrow)
    // ================================================================

    @Test
    @DisplayName("CT10/CT15 - Deve lançar ErroAoSalvarProdutoException quando o Repository falha")
    void deveLancarErroQuandoRepositoryFalharAoSalvar() {
        Produto produto = new Produto("Teclado", 129.90, 5);
        doThrow(new RuntimeException("Falha na conexão"))
                .when(repository).salvar(any(Produto.class));

        ErroAoSalvarProdutoException ex = assertThrows(
                ErroAoSalvarProdutoException.class,
                () -> service.salvar(produto));

        assertEquals("Falha ao salvar o produto", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("Falha na conexão", ex.getCause().getMessage());
        verify(repository).salvar(produto);
    }

    // ================================================================
    // Extras parametrizados (mesma regra, vários valores)
    // ================================================================

    @ParameterizedTest(name = "nome inválido [{0}] -> IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Extra - Nome nulo, vazio ou em branco nunca chega ao Repository")
    void naoDeveSalvarProdutoComNomeInvalido(String nome) {
        Produto produto = new Produto(nome, 49.90, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verifyNoInteractions(repository);
    }

    @ParameterizedTest(name = "preço inválido [{0}] -> IllegalArgumentException")
    @ValueSource(doubles = {0, -0.01, -10, -1000})
    @DisplayName("Extra - Preço zero ou negativo nunca chega ao Repository")
    void naoDeveSalvarProdutoComPrecoInvalido(double preco) {
        Produto produto = new Produto("Mouse", preco, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verifyNoInteractions(repository);
    }

    @ParameterizedTest(name = "quantidade inválida [{0}] -> IllegalArgumentException")
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    @DisplayName("Extra - Quantidade negativa nunca chega ao Repository")
    void naoDeveSalvarProdutoComQuantidadeInvalida(int quantidade) {
        Produto produto = new Produto("Mouse", 49.90, quantidade);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verifyNoInteractions(repository);
    }

    @ParameterizedTest(name = "preço {0} e quantidade {1} são válidos")
    @CsvSource({
            "0.01, 0",
            "0.01, 1",
            "49.90, 10",
            "9999.99, 1000"
    })
    @DisplayName("Extra - Combinações válidas (inclui fronteiras) são enviadas ao Repository")
    void deveSalvarCombinacoesValidas(double preco, int quantidade) {
        Produto produto = new Produto("Item", preco, quantidade);
        when(repository.salvar(produto)).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertSame(produto, resultado);
        verify(repository, times(1)).salvar(produto);
    }
}
