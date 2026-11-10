# Respostas — Atividades 4, 5 e 6

**Aluno:** Felipe Dias | Projeto: `sistema-produtos-mockito`

## Atividade 4 — Montando a classe de teste

```java
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;
}
```

### Para que serve cada anotação

- **`@ExtendWith(MockitoExtension.class)`**: liga o Mockito ao JUnit 5. Antes de cada teste ele cria os mocks e monta o service.
- **`@Mock`**: cria um objeto "de mentira" (dublê) do `ProdutoRepository`, sem banco de dados.
- **`@InjectMocks`**: cria o `ProdutoService` **de verdade** e coloca nele o mock do repository.

### Perguntas

**1. O que acontece se `@ExtendWith(MockitoExtension.class)` for esquecida?**
Ninguém inicializa os campos anotados. `repository` e `service` ficam `null` e os testes falham com `NullPointerException`.

**2. O objeto criado por `@InjectMocks` é mock ou instância real?**
É uma **instância real** de `ProdutoService`. Só as dependências dele (o repository) são mocks. Isso é necessário porque queremos testar a regra de negócio verdadeira.

**3. Como o mock chega até o service sem `new ProdutoService(repository)`?**
O Mockito faz isso sozinho: procura o construtor do `ProdutoService`, vê que ele pede um `ProdutoRepository`, encontra o mock desse tipo e o passa ao construtor (injeção por construtor).

## Atividade 5 — ProdutoServiceTest (CT01 a CT10)

| ID | O que testa | Método de teste |
|----|-------------|-----------------|
| CT01 | Salva produto válido (Mouse, 49.90, 10) com `when/thenReturn`, `assertEquals` e `verify` | `deveSalvarProdutoComPrecoValido` |
| CT02 | Rejeita preço negativo (-10); `never()` | `naoDeveSalvarProdutoComPrecoNegativo` |
| CT03 | Rejeita preço zero; `never()` | `naoDeveSalvarProdutoComPrecoIgualAZero` |
| CT04 | Rejeita nome vazio; `never()` | `naoDeveSalvarProdutoComNomeVazio` |
| CT05 | Rejeita nome nulo; `never()` | `naoDeveSalvarProdutoComNomeNulo` |
| CT06 | Rejeita quantidade -1; `never()` | `naoDeveSalvarProdutoComQuantidadeNegativa` |
| CT07 | Aceita quantidade 0; `verify` da chamada | `deveSalvarProdutoComQuantidadeIgualAZero` |
| CT08 | Busca produto existente (ID 1) | `deveRetornarProdutoQuandoExistir` |
| CT09 | Busca inexistente (ID 99): mock devolve `null` | `deveLancarExcecaoQuandoNaoEncontrado` |
| CT10 | Falha no Repository com `doThrow` (Teclado, 129.90, 5) | `deveLancarErroQuandoRepositoryFalharAoSalvar` |

Como funciona, em palavras simples: nos testes de dado inválido o Service lança `IllegalArgumentException` **antes** de falar com o Repository, e o `verify(repository, never())` prova isso.
Nos testes de sucesso, o mock é programado com `when(...).thenReturn(...)` e conferimos o retorno e a chamada. No CT10, `doThrow` faz o mock "quebrar", e o Service deve transformar o erro em `ErroAoSalvarProdutoException`.

## Atividade 6 — PedidoServiceTest (PED01 a PED08)

Dois `@Mock` (`ProdutoRepository` e `PedidoRepository`) e um `@InjectMocks` (`PedidoService`).

| ID | O que testa | Método de teste |
|----|-------------|-----------------|
| PED01 | Pedido válido: 49,90 × 2 = 99,80 e pedido salvo | `deveCalcularValorTotalDoPedido`, `deveSalvarOPedidoCalculadoNoRepositorio` |
| PED02 | Cada repositório chamado exatamente uma vez (`times(1)`) | `deveConsultarProdutoESalvarPedidoUmaVezCada`, `deveBuscarProdutoAntesDeSalvarPedido` |
| PED03 | Quantidade 0 | `naoDeveCriarPedidoComQuantidadeIgualAZero` |
| PED04 | Quantidade -3 | `naoDeveCriarPedidoComQuantidadeNegativa` |
| PED05 | Produto inexistente (ID 99) | `deveLancarExcecaoQuandoProdutoDoPedidoNaoExiste` |
| PED06 | 11 unidades com estoque 10 | `deveLancarExcecaoQuandoEstoqueInsuficiente` |
| PED07 | 10 unidades com estoque 10 (aceita; total 499,00) | `deveAceitarPedidoComQuantidadeIgualAoEstoque` |
| PED08 | Nenhum erro salva pedido: `verify(pedidoRepository, never()).salvar(any(Pedido.class))` | verificado em PED03 a PED06 e em `naoDeveSalvarPedidoEmNenhumCenarioDeErro` |

Além disso, a Atividade 7 está testada: falha do `PedidoRepository` com `doThrow` (vira `ErroAoSalvarPedidoException`), `doNothing` em método `void` e falha do `ProdutoRepository`.

## Resultado da execução

**Os testes não foram executados no ambiente em que o projeto foi gerado**, porque ele não tinha acesso ao Maven Central para baixar JUnit e Mockito. Apenas as classes de `src/main` foram compiladas (sem erros).
Para registrar o resultado real, rode `mvn clean test` (ou `Run 'All Tests'` no IntelliJ) e anote aqui:

- Total de testes: ______  Aprovados: ______  Falhas: ______
