# sistema-produtos-mockito

**Aluno:** Felipe Dias — Técnico em Desenvolvimento de Sistemas (Noite) — 10/11/2026
**Atividade:** Testes Unitários com JUnit 5 e Mockito

## 1. Descrição do sistema

Sistema da **TechStore Distribuidora** para cadastro de produtos e criação de pedidos, em camadas:

| Camada | Responsabilidade | Neste projeto |
|--------|------------------|---------------|
| Controller | Recebe requisições HTTP | Não implementado (não entra no teste unitário) |
| Service | Regras de negócio | `ProdutoService` e `PedidoService` (classes **reais** testadas) |
| Repository | Acesso ao banco | Interfaces; nos testes viram **mocks** |
| Banco de dados | Guarda os dados | Nunca é acessado |

## 2. Objetivo da atividade

Testar as regras de negócio dos Services **sem banco de dados real**, trocando os Repositories por mocks do Mockito,
programando o que eles devolvem (`when/thenReturn`, `doThrow`) e conferindo as chamadas (`verify`, `never`, `times`).

## 3. Tecnologias

- Java 17 ou superior
- Maven (com Maven Surefire Plugin 3.5.2, compatível com JUnit 5)
- JUnit Jupiter 5.11.4
- Mockito JUnit Jupiter 5.14.2
- IntelliJ IDEA

## 4. Estrutura do projeto

```
sistema-produtos-mockito/
├── pom.xml
├── README.md
├── RESPOSTAS_ATIVIDADES_4_5_6.md
├── .gitignore
├── docs/                                   (respostas completas e registros de execução)
│   ├── Respostas_Felipe_Dias.docx
│   ├── 01-Respostas-das-Atividades.md
│   └── 02-Registros-de-Execucao.md
└── src/
    ├── main/java/br/com/techstore/
    │   ├── App.java      (programa de console: cadastro e pedidos na mão)
    │   ├── model/        Produto.java, Pedido.java
    │   ├── repository/   ProdutoRepository.java, PedidoRepository.java,
    │   │                 ProdutoRepositoryEmMemoria.java, PedidoRepositoryEmMemoria.java
    │   ├── service/      ProdutoService.java, PedidoService.java
    │   └── exception/    ProdutoNaoEncontradoException.java, ErroAoSalvarProdutoException.java,
    │                     EstoqueInsuficienteException.java, ErroAoSalvarPedidoException.java
    └── test/java/br/com/techstore/service/
        ├── ProdutoServiceTest.java
        └── PedidoServiceTest.java
```

## 5. Explicação das classes

- **Produto**: nome, preço e quantidade em estoque.
- **Pedido**: produto, quantidade pedida e valor total.
- **ProdutoRepository** (interface): `salvar(Produto)` e `buscarPorId(Long)`; devolve `null` se não achar.
- **PedidoRepository** (interface): `salvar(Pedido)`, método `void`.
- **ProdutoService**:
  - `salvar`: rejeita produto nulo, nome nulo/vazio/em branco, preço ≤ 0 e quantidade negativa (`IllegalArgumentException`);
    se o Repository lançar `RuntimeException`, lança `ErroAoSalvarProdutoException`.
  - `buscarPorId`: devolve o produto ou lança `ProdutoNaoEncontradoException` quando o Repository devolve `null`.
- **PedidoService** (`criarPedido`): valida quantidade > 0, busca o produto, confere o estoque, calcula
  `preço × quantidade`, salva o pedido; se o `PedidoRepository` falhar, lança `ErroAoSalvarPedidoException`.
- **ProdutoRepositoryEmMemoria / PedidoRepositoryEmMemoria**: guardam os dados na memória; usadas só pelo `App` (os testes usam mocks).
- **App**: menu de console para cadastrar produtos, listar, buscar e criar pedidos.
- **Exceções**: todas estendem `RuntimeException`.

## 6. Explicação dos testes

Todas as classes de teste usam `@ExtendWith(MockitoExtension.class)`, `@Mock` nos repositórios e `@InjectMocks` no Service real.
Cada teste segue **Arrange – Act – Assert** e o `@DisplayName` traz o ID do plano.

| Classe | Cenários |
|--------|----------|
| `ProdutoServiceTest` | CT01 a CT10 (plano da Atividade 2), CT11 a CT16 (analista) e extras parametrizados |
| `PedidoServiceTest` | PED01 a PED08 e Atividade 7 (`doThrow`, `doNothing`, falha no `ProdutoRepository`) |

Recursos usados: `@Mock`, `@InjectMocks`, `when().thenReturn()`, `doThrow().when()`, `verify()`, `never()`, `times(1)`,
`any()`, `assertThrows()`, `assertEquals()`, `assertNotNull()`, além de `ArgumentCaptor`, `InOrder` e `@ParameterizedTest`.

## 6.1. Programa de console (`App`) — cadastro de produtos na mão

Além dos testes, o projeto tem um programa de console para você usar o sistema de verdade:
`src/main/java/br/com/techstore/App.java`. Ele usa os **Services reais** com repositórios **em memória**
(`ProdutoRepositoryEmMemoria` e `PedidoRepositoryEmMemoria`), então não precisa de banco de dados.
Os dados ficam só na memória: ao fechar o programa, tudo é apagado.

Menu do programa:

```
1 - Cadastrar produto      (digite nome, preço e quantidade)
2 - Listar produtos
3 - Buscar produto por ID
4 - Criar pedido           (digite o ID do produto e a quantidade)
5 - Listar pedidos
0 - Sair
```

- O ID do produto é gerado automaticamente (1, 2, 3...) e aparece depois do cadastro e na listagem.
- O preço aceita vírgula ou ponto (`49,90` ou `49.90`).
- Dados inválidos (preço 0, nome vazio, estoque insuficiente etc.) mostram a mensagem de erro das regras do Service.
- Criar um pedido **não diminui o estoque** do produto, porque o `PedidoService` do enunciado não faz isso.

**Como rodar no IntelliJ:** abra `App.java`, clique na seta verde ao lado de `public static void main` e escolha
**Run 'App.main()'**. Digite as opções na janela *Run* (parte de baixo) e tecle Enter.

**Como rodar pelo terminal** (dentro da pasta `sistema-produtos-mockito`):

```
mvn compile
java -cp target/classes br.com.techstore.App
```

Se os acentos aparecerem errados no Windows, rode antes `chcp 65001`.

## 7. Como abrir no IntelliJ IDEA

1. Extraia o ZIP.
2. `File > Open...` e selecione a pasta `sistema-produtos-mockito` (a que tem o `pom.xml`).
3. Escolha **Open as Project** e **Trust Project**.
4. Aguarde o Maven baixar as dependências (precisa de internet na primeira vez); se preciso,
   botão direito no `pom.xml` > `Maven > Reload project`.
5. Confira o JDK 17+ em `File > Project Structure > Project SDK`.

## 8. Como executar os testes pelo terminal

Dentro da pasta `sistema-produtos-mockito`:

```
mvn clean test
```

## 9. Como executar os testes individualmente

- **No IntelliJ:** clique na seta verde ao lado do `@Test` (um teste) ou da classe (todos os testes da classe).
- **No terminal, uma classe:**
  ```
  mvn test -Dtest=ProdutoServiceTest
  ```
- **No terminal, um método:**
  ```
  mvn test -Dtest=ProdutoServiceTest#deveSalvarProdutoComPrecoValido
  ```
- **Duas classes:** `mvn test -Dtest=ProdutoServiceTest,PedidoServiceTest`

## Observação sobre os resultados

Os resultados reais dependem da execução na sua máquina. O ambiente onde este projeto foi gerado não tinha acesso ao
Maven Central, então os testes **não foram executados lá**; apenas as classes principais foram compiladas.
