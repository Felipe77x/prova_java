---
title: "Atividade — Testes Unitários com Mockito | JUnit 5"
author: "Felipe Dias — Técnico em Desenvolvimento de Sistemas (Noite) — 10/11/2026"
---

# Atividade 1 — Análise do Código

**1. Qual é a dependência da classe `ProdutoService`? Como ela é recebida?**
A dependência é o `ProdutoRepository`. Ela é recebida por **injeção via construtor**
(`public ProdutoService(ProdutoRepository repository)`) e guardada em um campo `final`.

**2. Por que o fato de `ProdutoRepository` ser uma interface facilita o uso de mocks?**
A interface define só o contrato (assinaturas), sem implementação nem ligação com banco. O Mockito gera em
tempo de execução um objeto que implementa essa interface e responde como programarmos. O Service só conhece o
tipo `ProdutoRepository`, então não percebe se recebeu a implementação real (JDBC/JPA) ou um mock.

**3. No teste, qual objeto deve ser mock e qual deve ser a classe real?**
- **Mock:** `ProdutoRepository` — é a dependência externa (acessa o banco); queremos isolá-la e controlar o que devolve.
- **Real:** `ProdutoService` — é a unidade sob teste; é a regra de negócio que queremos validar.

**4. Em quais situações `repository.salvar()` não deve ser chamado?**
Quando a validação falha: produto `null`; nome nulo, vazio ou em branco; preço menor ou igual a zero; quantidade
negativa. Todos lançam `IllegalArgumentException` antes de chegar ao `try`.

**5. O que o mock devolve se o teste não programar o método com `when()`? Que problema isso causa em `buscarPorId()`?**
Devolve o valor padrão do tipo: `null` para objetos, `0` para numéricos, `false` para `boolean`, coleções vazias para
`List/Set/Map`. Em `buscarPorId()`, o mock devolveria `null` e o Service lançaria `ProdutoNaoEncontradoException`.
Assim o teste de "produto existente" falharia por um motivo enganoso (faltou programar o mock) e o de "inexistente"
passaria sem declarar a intenção. Por isso programamos sempre de forma explícita com `when(...).thenReturn(...)`.

**6. Por que não devemos mockar a própria classe `ProdutoService`?**
Porque o teste executaria código falso em vez da regra de negócio real: só verificaríamos o que nós mesmos programamos
no mock. O teste passaria sempre e não provaria nada.

# Atividade 2 — Plano de Teste Unitário com Mocks

| ID | Req. | Cenário | Configuração do mock | Resultado esperado | Verificação (verify) |
|----|------|---------|----------------------|--------------------|----------------------|
| CT01 | RF01 | Produto válido ("Mouse", 49.90, 10) | `when(repository.salvar(produto)).thenReturn(produto)` | Retorna o produto salvo | `verify(repository).salvar(produto)` |
| CT02 | RF02 | Preço negativo (-10) | Não precisa | `IllegalArgumentException` | `verify(repository, never()).salvar(any())` |
| CT03 | RF02 | Preço igual a zero | Não precisa | `IllegalArgumentException` ("Preço deve ser maior que zero") | `verify(repository, never()).salvar(any())` |
| CT04 | RF02 | Nome vazio ("") | Não precisa | `IllegalArgumentException` ("Nome do produto é obrigatório") | `verify(repository, never()).salvar(any())` |
| CT05 | RF02 | Nome nulo | Não precisa | `IllegalArgumentException` | `verify(repository, never()).salvar(any())` |
| CT06 | RF02 | Quantidade negativa (-1) | Não precisa | `IllegalArgumentException` ("Quantidade não pode ser negativa") | `verify(repository, never()).salvar(any())` |
| CT07 | RF02 | Quantidade igual a zero | `when(repository.salvar(produto)).thenReturn(produto)` | Aceitar (estoque zerado é permitido) | `verify(repository).salvar(produto)` |
| CT08 | RF03 | Buscar produto existente (ID 1) | `when(repository.buscarPorId(1L)).thenReturn(produto)` | Retorna o produto | `verify(repository).buscarPorId(1L)` |
| CT09 | RF03 | Buscar produto inexistente (ID 99) | `when(repository.buscarPorId(99L)).thenReturn(null)` | `ProdutoNaoEncontradoException` | `verify(repository).buscarPorId(99L)` |
| CT10 | RF04 | Falha de conexão ao salvar | `doThrow(new RuntimeException("Falha na conexão")).when(repository).salvar(any(Produto.class))` | `ErroAoSalvarProdutoException` (com a causa original) | `verify(repository).salvar(produto)` |
| CT11 | RF02 | Preço 0 (fronteira inválida) | Não precisa | `IllegalArgumentException` | `never()` (mesmo teste do CT03) |
| CT12 | RF02 | Preço 0.01 (menor preço válido) | `when(repository.salvar(produto)).thenReturn(produto)` | Aceitar | `verify(repository).salvar(produto)` |
| CT13 | RF02 | Nome em branco ("   ") | Não precisa | `IllegalArgumentException` | `verifyNoInteractions(repository)` |
| CT14 | RF03 | Produto não encontrado | `thenReturn(null)` | `ProdutoNaoEncontradoException` | `verify(repository).buscarPorId(99L)` (mesmo teste do CT09) |
| CT15 | RF04 | Banco fora do ar | `doThrow(RuntimeException)` | `ErroAoSalvarProdutoException` | `verify(repository).salvar(produto)` (mesmo teste do CT10) |
| CT16 | RF01 | Matcher `any(Produto.class)` | `when(repository.salvar(any(Produto.class))).thenReturn(produto)` | Retorna o produto | `verify(repository, times(1)).salvar(any(Produto.class))` |
| Extra | RF02 | Produto `null` (cenário não previsto no plano) | Não precisa | `IllegalArgumentException` | `verifyNoInteractions(repository)` |
| Extra | RF02 | Valores variados inválidos (nome " ", "\t"; preço -0.01, -1000; quantidade -100) | Não precisa | `IllegalArgumentException` | `verifyNoInteractions(repository)` |

# Atividade 3 — Cenários do Analista de Testes

**CT11 — Produto com preço igual a zero.** O Service **rejeita**, porque a regra é "preço maior que zero".
Quem lança a exceção é a linha `if (produto.getPreco() <= 0) throw new IllegalArgumentException("Preço deve ser maior que zero");`.
Usamos `verify(repository, never())` porque rejeitar não basta: precisamos provar que o dado inválido **não chegou** ao Repository (não seria gravado no banco).

**CT12 — Produto com preço 0.01.** Está **dentro** da regra (0.01 > 0) e deve ser aceito. É um **teste de fronteira**:
0.01 é o menor valor válido, vizinho imediato do 0 (inválido). Erros como `<` no lugar de `<=` aparecem exatamente aí.

**CT13 — Nome em branco ("   ").** `isBlank()` retorna **true** (só espaços), então a validação lança `IllegalArgumentException`.
O mock do Repository **não chega a ser usado**; por isso o teste usa `verifyNoInteractions(repository)`.

**CT14 — Produto não encontrado.** O mock deve devolver **`null`**: `when(repository.buscarPorId(99L)).thenReturn(null)`.
O `verify(repository).buscarPorId(99L)` é importante porque prova que o Service **realmente consultou** o Repository com o ID certo.
Sem ele, a exceção poderia vir de outro ponto e o teste ainda passaria.

**CT15 — Banco de dados fora do ar.** Simulamos com o mock, sem derrubar banco algum:
`doThrow(new RuntimeException("Falha na conexão")).when(repository).salvar(any(Produto.class))`.
O Service lança **`ErroAoSalvarProdutoException`** (encapsulando a causa original) para quem o chamou.

**CT16 — Objeto específico × `any()`.**
`when(repository.salvar(produto))` só vale quando o argumento é **igual** (`equals`) ao `produto` informado; como `Produto`
não sobrescreve `equals`, vale a **mesma instância**. `when(repository.salvar(any(Produto.class)))` vale para **qualquer** `Produto`.
O objeto específico é mais adequado quando queremos garantir que o Service repassou *aquele* objeto (CT01).
O `any()` é mais adequado quando o objeto é criado dentro do Service (como o `Pedido` em `criarPedido`) ou quando o valor exato não importa (como no `doThrow`).

# Atividade 4 — Montando a Classe de Teste

```java
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;
}
```

**Papel de cada anotação**

- `@ExtendWith(MockitoExtension.class)`: liga o Mockito ao JUnit 5; cria os mocks antes de cada teste e avisa sobre stubs não usados.
- `@Mock`: cria o objeto "de mentira" do `ProdutoRepository`.
- `@InjectMocks`: cria a instância **real** do `ProdutoService` e injeta nela os mocks.

**1. E se `@ExtendWith(MockitoExtension.class)` for esquecida?** Os campos `@Mock` e `@InjectMocks` ficam `null` (ninguém os inicializa)
e os testes quebram com `NullPointerException`.

**2. O objeto de `@InjectMocks` é mock ou instância real?** É uma **instância real** de `ProdutoService`; só as dependências dele são mocks.
Por isso a regra de negócio real é executada.

**3. Como o mock chega ao `service` sem `new ProdutoService(repository)`?** O Mockito faz a injeção por construtor: usa o
construtor com mais parâmetros e passa os mocks do tipo correspondente. Se não houver construtor adequado, tenta por setter ou campo.

# Atividade 5 — Implementação

Implementada em `src/test/java/br/com/techstore/service/ProdutoServiceTest.java` (CT01 a CT16, mais testes extras).
Complemento do CT10 (as lacunas do enunciado):

```java
assertThrows(ErroAoSalvarProdutoException.class, () -> service.salvar(produto));
verify(repository).salvar(produto);
```

# Atividade 6 — PedidoService com duas dependências

Implementada em `PedidoServiceTest.java` (dois `@Mock` e um `@InjectMocks`).

| ID | Cenário | Configuração dos mocks | Resultado esperado | Método de teste |
|----|---------|------------------------|--------------------|-----------------|
| PED01 | Pedido válido: Mouse R$ 49,90, estoque 10, quantidade 2 | `produtoRepository.buscarPorId(1L)` retorna o produto | Valor total = 99,80 e pedido salvo | `deveCalcularValorTotalDoPedido`, `deveSalvarOPedidoCalculadoNoRepositorio` |
| PED02 | Pedido válido | Igual ao PED01 | `verify` dos dois repositórios com `times(1)` | `deveConsultarProdutoESalvarPedidoUmaVezCada`, `deveBuscarProdutoAntesDeSalvarPedido` |
| PED03 | Quantidade igual a zero | Não precisa | `IllegalArgumentException` | `naoDeveCriarPedidoComQuantidadeIgualAZero` |
| PED04 | Quantidade negativa (-3) | Não precisa | `IllegalArgumentException` | `naoDeveCriarPedidoComQuantidadeNegativa` |
| PED05 | Produto inexistente (ID 99) | `buscarPorId(99L)` retorna `null` | `ProdutoNaoEncontradoException` | `deveLancarExcecaoQuandoProdutoDoPedidoNaoExiste` |
| PED06 | Quantidade 11 com estoque 10 | `buscarPorId(1L)` retorna produto com estoque 10 | `EstoqueInsuficienteException` | `deveLancarExcecaoQuandoEstoqueInsuficiente` |
| PED07 | Quantidade 10 com estoque 10 | Igual ao PED06 | Aceitar (fronteira); total 499,00 | `deveAceitarPedidoComQuantidadeIgualAoEstoque` |
| PED08 | Qualquer erro (PED03 a PED06) | — | `verify(pedidoRepository, never()).salvar(any())` | `never()` em PED03 a PED06 e `naoDeveSalvarPedidoEmNenhumCenarioDeErro` (reúne os quatro erros) |

# Atividade 7 — Simulando falhas com `doThrow`

**Cenário A — Banco fora do ar ao salvar o pedido.** Com `doThrow(new RuntimeException("Falha na conexão")).when(pedidoRepository).salvar(any(Pedido.class))`
e a chamada `service.criarPedido(1L, 2)` dentro de um `assertThrows`, a exceção que sai é a **`ErroAoSalvarPedidoException`**: o `PedidoService` **trata** o erro
(captura a `RuntimeException` do Repository e lança a exceção própria, preservando a causa). Na versão original do enunciado, sem `try/catch`, a `RuntimeException` do mock chegaria direto ao chamador.
Teste: `deveLancarErroAoSalvarPedidoQuandoRepositoryFalha`.

**Cenário B — `when()` em método `void`.** `when(pedidoRepository.salvar(any())).thenReturn(...)` **não compila**: `salvar()` é `void`,
então a expressão não tem valor e não pode ser passada para `when(T)`. A forma correta usa a família `do...()`:
`doThrow(new RuntimeException()).when(pedidoRepository).salvar(any());` (ou `doNothing()` para o caso normal).
Teste de apoio: `devePermitirDoNothingEmMetodoVoid`.

**Cenário C — Falha no `ProdutoRepository`.** `produtoRepository.buscarPorId(1L)` é programado com `thenThrow(new RuntimeException(...))`.
O `pedidoRepository.salvar()` **não deve ser chamado**, porque o erro interrompe o método antes. Comprovado com
`verify(pedidoRepository, never()).salvar(any(Pedido.class))`. Teste: `naoDeveSalvarPedidoQuandoProdutoRepositoryFalha`.

**Cenário D (desafio) — Melhorando o Service.** O `PedidoService.criarPedido()` foi alterado para capturar a falha do `PedidoRepository` e lançar a
`ErroAoSalvarPedidoException` (com a causa original). O teste que comprova o tratamento é o mesmo do Cenário A:
`deveLancarErroAoSalvarPedidoQuandoRepositoryFalha`.

# Atividade 8 — Perguntas do Analista de Testes

Três testes analisados: **sucesso** (PED01 `deveCalcularValorTotalDoPedido`), **exceção** (CT09 `deveLancarExcecaoQuandoNaoEncontrado`)
e **doThrow** (CT10 `deveLancarErroQuandoRepositoryFalharAoSalvar`).

| # | Pergunta | Sucesso — PED01 | Exceção — CT09 | doThrow — CT10 |
|---|----------|-----------------|----------------|----------------|
| 1 | Requisito validado | RF05 | RF03 | RF04 |
| 2 | Método do Service testado | `PedidoService.criarPedido` | `ProdutoService.buscarPorId` | `ProdutoService.salvar` |
| 3 | Dependência mockada | `ProdutoRepository` | `ProdutoRepository` | `ProdutoRepository` |
| 4 | Como o mock foi programado | `when/thenReturn` (devolve o produto) | `when/thenReturn(null)` | `doThrow(RuntimeException)` |
| 5 | Condição de entrada | ID 1, quantidade 2, Mouse R$ 49,90, estoque 10 | ID 99 | Produto válido ("Teclado", 129.90, 5) |
| 6 | Resultado esperado | Pedido com valor total 99,80 | `ProdutoNaoEncontradoException` | `ErroAoSalvarProdutoException` |
| 7 | Resultado obtido / status | *Preencher após rodar no IntelliJ (esperado: aprovado)* | *idem* | *idem* |
| 8 | Interação confirmada com `verify` | (na versão com captor) `salvar` do `PedidoRepository` capturado; em PED02, `times(1)` nos dois repositórios | `buscarPorId(99L)` 1 vez; sem `never()` | `salvar(produto)` 1 vez |
| 9 | Por que não precisa de banco real | Os repositórios são mocks; o teste valida só cálculo e regras do Service | O mock devolve `null` por conta própria | O mock simula a falha, sem derrubar banco algum |
| 10 | Tipo do caso | Normal | Exceção | Exceção |
| 11 | Se falhar, o problema está no Service ou no Repository? | No **Service** (cálculo ou fluxo): o Repository é um mock e não tem lógica real | No **Service** (não trata o `null`) | No **Service** (não encapsula a exceção) |
| 12 | Repetível e independente? | Sim: sem estado compartilhado; os mocks são recriados a cada teste | Sim | Sim |

# Atividade 9 — Análise de Cobertura e Boas Práticas

- Todos os requisitos RF01 a RF05 têm ao menos um teste: **sim** (RF01: CT01, CT07, CT12, CT16; RF02: CT02 a CT07, CT11 a CT13, extras; RF03: CT08, CT09; RF04: CT10; RF05: PED01 a PED08, Atividade 7).
- Caminho de sucesso e caminhos de exceção de cada método: **sim**.
- Testes de dados inválidos confirmam que o Repository não foi chamado (`never()` ou `verifyNoInteractions`): **sim**.
- Retorno `null` do Repository (produto inexistente): **sim** (CT09, PED05).
- Falha do Repository simulada com `doThrow`: **sim** (CT10, Atividade 7).
- Valores de fronteira (preço 0 e 0.01; quantidade igual e maior que o estoque): **sim** (CT03, CT12, PED06, PED07, além de quantidade 0 no CT07).
- Algum teste mockou a própria classe testada: **não** (`ProdutoService` e `PedidoService` são sempre `@InjectMocks`).
- Cada teste programa só o necessário: **sim**; o `MockitoExtension` em modo estrito acusa stubs não usados.
- Nomes descrevem o cenário: **sim**, e o `@DisplayName` traz o ID do plano.
- **Cenários relevantes ainda não cobertos:**
  - `PedidoService` com `produtoId` nulo, ou produto com preço/estoque inválido: o Service não valida isso (seria melhoria do código).
  - Arredondamento de `double` em valores monetários: em produção o ideal é `BigDecimal`.
  - `buscarPorId(null)`: depende do contrato do Repository real, é assunto de teste de integração.
  - Concorrência no estoque (dois pedidos ao mesmo tempo): não é testável com mocks e exige teste de integração.

# Desafio Final — Análise do Analista de Testes

> "A equipe possui 18 testes automatizados com Mockito e todos estão passando. Portanto, o sistema vai funcionar perfeitamente com o banco de dados de produção."

**A afirmação está incorreta.** Ela confunde a **quantidade de testes aprovados** com a **garantia de funcionamento do sistema integrado**.

1. **O que um mock realmente testa.** Um mock só devolve o que o teste mandou. Com `when(...).thenReturn(...)`, validamos que *a lógica do Service reage corretamente
   ao comportamento que nós supomos* do Repository, e não que o Repository real se comporta assim. Se a suposição estiver errada (por exemplo, o Repository real lança
   exceção em vez de devolver `null`), os testes continuam verdes e o sistema quebra.

2. **Isolamento do Service.** O isolamento é a força do teste unitário (rápido, determinístico, aponta defeitos na regra de negócio) e também seu limite:
   tudo o que está **fora** do Service (SQL/JPA, mapeamento de entidades, transações, constraints, índices, driver, pool e configuração de conexão) **nunca foi executado**.

3. **Regra de negócio × Repository real.** Os testes provam validações (preço, nome, quantidade), o cálculo (preço × quantidade) e o tratamento de erros.
   Não provam que `salvar()` persiste de fato, que `buscarPorId()` encontra o registro certo, que os tipos (`double` × `NUMERIC`) são mapeados corretamente
   nem que a transação faz commit e rollback.

4. **Cenários de exceção e de fronteira.** "18 testes passando" não diz **quais** cenários foram cobertos; faltando fronteira (preço 0 e 0.01, quantidade igual ao estoque)
   ou exceção (banco fora, `null`), há lacunas escondidas. Mesmo cobrindo, o `doThrow` simula uma falha *imaginada*: falhas reais (timeout, deadlock, violação de unicidade) têm outros formatos.

5. **Necessidade de outros tipos de teste.**
   - **Integração** (Service + Repository real, com banco de teste ou Testcontainers): valida SQL, mapeamento e transações.
   - **Contrato/API e ponta a ponta:** valida Controller → Service → Repository → banco.
   - **Desempenho e concorrência:** estoque com pedidos simultâneos, volume de dados.
   - **Ambiente de homologação semelhante à produção:** configuração, permissões, migrações.

**Conclusão:** os testes dão confiança de que a **regra de negócio do Service** está correta *sob as premissas* programadas nos mocks. Não permitem concluir que o sistema funcionará
"perfeitamente" em produção; essa conclusão exige testes de integração e de sistema com o banco real.
