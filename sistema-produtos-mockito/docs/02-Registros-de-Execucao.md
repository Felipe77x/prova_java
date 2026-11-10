# Registros de Execução (item 15)

**Aluno:** Felipe Dias | **Projeto:** sistema-produtos-mockito

> **Importante:** a coluna "Resultado Obtido" deve ser preenchida depois de rodar os testes no IntelliJ IDEA
> (`Run 'All Tests'`). Está marcada como **a confirmar** porque o ambiente em que o projeto foi gerado não
> tinha acesso ao Maven Central e os testes não puderam ser executados lá. Tire os prints da janela *Run*
> e anexe na seção final.

## ProdutoServiceTest

| ID | Requisito | Método testado | Mock(s) | Configuração do mock | Cenário / Entrada | Resultado esperado | Verificação (verify) | Resultado obtido / Status |
|----|-----------|----------------|---------|----------------------|-------------------|--------------------|----------------------|---------------------------|
| CT01 | RF01 | `ProdutoService.salvar` | `ProdutoRepository` | `when(salvar(produto)).thenReturn(produto)` | Produto("Mouse", 49.90, 10) | Retorna o produto salvo | `verify(repository).salvar(produto)` | A confirmar |
| CT02 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Preço -10 | `IllegalArgumentException` | `never()` | A confirmar |
| CT03 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Preço 0 | `IllegalArgumentException` | `never()` | A confirmar |
| CT04 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Nome "" | `IllegalArgumentException` | `never()` | A confirmar |
| CT05 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Nome `null` | `IllegalArgumentException` | `never()` | A confirmar |
| CT06 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Quantidade -1 | `IllegalArgumentException` | `never()` | A confirmar |
| CT07 | RF02 | `salvar` | `ProdutoRepository` | `when(salvar(produto)).thenReturn(produto)` | Quantidade 0 | Aceita | `verify(repository).salvar(produto)` | A confirmar |
| CT08 | RF03 | `buscarPorId` | `ProdutoRepository` | `when(buscarPorId(1L)).thenReturn(produto)` | ID 1 | Retorna o produto | `verify(repository).buscarPorId(1L)` | A confirmar |
| CT09 | RF03 | `buscarPorId` | `ProdutoRepository` | `when(buscarPorId(99L)).thenReturn(null)` | ID 99 | `ProdutoNaoEncontradoException` | `verify(repository).buscarPorId(99L)` | A confirmar |
| CT10 | RF04 | `salvar` | `ProdutoRepository` | `doThrow(RuntimeException).when(repository).salvar(any())` | Produto válido | `ErroAoSalvarProdutoException` | `verify(repository).salvar(produto)` | A confirmar |
| CT12 | RF02 | `salvar` | `ProdutoRepository` | `when(salvar(produto)).thenReturn(produto)` | Preço 0.01 | Aceita | `verify(repository).salvar(produto)` | A confirmar |
| CT13 | RF02 | `salvar` | `ProdutoRepository` | nenhuma | Nome "   " | `IllegalArgumentException` | `verifyNoInteractions(repository)` | A confirmar |
| CT16 | RF01 | `salvar` | `ProdutoRepository` | `when(salvar(any(Produto.class))).thenReturn(produto)` | Produto("Monitor", 899, 3) | Retorna o produto | `times(1)` com `any()` | A confirmar |

## PedidoServiceTest

| ID | Requisito | Método testado | Mock(s) | Configuração do mock | Cenário / Entrada | Resultado esperado | Verificação (verify) | Resultado obtido / Status |
|----|-----------|----------------|---------|----------------------|-------------------|--------------------|----------------------|---------------------------|
| PED01 | RF05 | `PedidoService.criarPedido` | `ProdutoRepository`, `PedidoRepository` | `when(buscarPorId(1L)).thenReturn(produto)` | ID 1, quantidade 2 | Total 99,80 | `ArgumentCaptor` no `salvar` | A confirmar |
| PED02 | RF05 | `criarPedido` | idem | idem | ID 1, quantidade 2 | Pedido criado | `times(1)` nos dois repositórios; `InOrder` | A confirmar |
| PED03 | RF05 | `criarPedido` | idem | nenhuma | Quantidade 0 | `IllegalArgumentException` | `never()` em `pedidoRepository.salvar` | A confirmar |
| PED04 | RF05 | `criarPedido` | idem | nenhuma | Quantidade -3 | `IllegalArgumentException` | `never()` | A confirmar |
| PED05 | RF05 | `criarPedido` | idem | `when(buscarPorId(99L)).thenReturn(null)` | ID 99 | `ProdutoNaoEncontradoException` | `never()` | A confirmar |
| PED06 | RF05 | `criarPedido` | idem | `when(buscarPorId(1L)).thenReturn(produto)` | Quantidade 11, estoque 10 | `EstoqueInsuficienteException` | `never()` | A confirmar |
| PED07 | RF05 | `criarPedido` | idem | idem | Quantidade 10, estoque 10 | Aceita (fronteira) | `times(1)` em `salvar` | A confirmar |
| PED08 | RF05 | `criarPedido` | idem | `when(buscarPorId(1L))` e `when(buscarPorId(99L))` | Quantidade 0, -3, ID 99, quantidade 11 | Quatro exceções | `never()` em `salvar(any(Pedido.class))` | A confirmar |
| ATV7-A/D | RF05 | `criarPedido` | `ProdutoRepository`, `PedidoRepository` | `doThrow(RuntimeException).when(pedidoRepository).salvar(any())` | ID 1, quantidade 2 | `ErroAoSalvarPedidoException` | `times(1)` em `salvar` | A confirmar |
| ATV7-B | RF05 | `criarPedido` | idem | `doNothing().when(pedidoRepository).salvar(any())` | ID 1, quantidade 2 | Pedido criado | `verify(pedidoRepository).salvar(pedido)` | A confirmar |
| ATV7-C | RF05 | `criarPedido` | idem | `when(produtoRepository.buscarPorId(1L)).thenThrow(...)` | ID 1, quantidade 2 | `RuntimeException` | `never()` em `salvar` | A confirmar |

## Evidências (prints do IntelliJ IDEA — RNF06)

1. Print da janela **Run** com `ProdutoServiceTest` (todos verdes).
2. Print da janela **Run** com `PedidoServiceTest` (todos verdes).
3. Print do **Run 'All Tests'** completo com o total de testes aprovados.
