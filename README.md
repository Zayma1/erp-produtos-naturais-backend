# ERP Produtos Naturais - Backend

API REST em **Java 17 + Spring Boot 3** para o sistema de gerenciamento de estoque da **Vila Verde**.

Escopo desta etapa (MVP): **Produto + Estoque** (sem autenticação).

## Stack
- Java 17, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL
- Lombok
- Springdoc OpenAPI (Swagger)

## Pré-requisitos
- JDK 17+
- Maven 3.9+ (ou use o wrapper, se adicionar `mvnw`)
- PostgreSQL 14+ rodando localmente

## Configuração do banco
Crie o banco antes de subir a aplicação:

```sql
CREATE DATABASE vilaverde_erp;
```

Ajuste usuário/senha em `src/main/resources/application.yml` se necessário. O Hibernate está com
`ddl-auto: update`, então as tabelas são criadas/atualizadas automaticamente ao subir a aplicação.

## Rodando o projeto

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Documentação interativa (Swagger) em
`http://localhost:8080/swagger-ui.html`.

## Modelo implementado
- `categoria`, `unidade_medida`, `loja`, `fornecedor`, `transacao` — cadastros de apoio
- `produto` — cadastro geral do produto (nome, categoria, unidade de medida, status)
- `produto_sku` — SKU do produto por loja (preço, código de barras, status)
- `produto_loja` — vínculo produto x loja disponível
- `estoque_produto` — ledger de movimentações de estoque (entrada/saída/ajuste), com
  quantidade anterior/atual calculada automaticamente pela regra de negócio no
  `EstoqueService`

## Principais endpoints

| Recurso | Endpoint |
|---|---|
| Categorias | `GET/POST/PUT/DELETE /api/categorias` |
| Unidades de medida | `GET/POST/PUT/DELETE /api/unidades-medida` |
| Lojas | `GET/POST/PUT/DELETE /api/lojas` |
| Fornecedores | `GET/POST/PUT/DELETE /api/fornecedores` |
| Transações (tipos de movimentação) | `GET/POST/PUT/DELETE /api/transacoes` |
| Produtos | `GET/POST/PUT/DELETE /api/produtos` |
| SKUs | `GET/POST/PUT/DELETE /api/produto-skus?idProduto=` |
| Produto x Loja | `GET/POST/DELETE /api/produto-lojas` |
| Estoque (listar tudo) | `GET /api/estoque` |
| Estoque (histórico do produto) | `GET /api/estoque/produto/{idProduto}` |
| Estoque (saldo atual) | `GET /api/estoque/saldo?idProduto=&idLoja=` |
| Registrar movimentação | `POST /api/estoque/movimentacao` |

### Exemplo: registrar entrada de estoque

```json
POST /api/estoque/movimentacao
{
  "idProduto": "uuid-do-produto",
  "idLoja": "uuid-da-loja",
  "idFornecedor": "uuid-do-fornecedor",
  "idTransacao": "uuid-da-transacao-entrada",
  "quantidade": 50,
  "motivo": "Compra mensal"
}
```

Antes de registrar movimentações, é preciso cadastrar ao menos:
1. Uma `categoria` e uma `unidade_medida` (para criar um `produto`)
2. Uma `loja`
3. Uma `transacao` para cada tipo (ex: sigla `ENT`, nome "Entrada", tipo `ENTRADA`;
   sigla `SAI`, nome "Saída", tipo `SAIDA`; sigla `AJU`, nome "Ajuste", tipo `AJUSTE`)
4. Um `fornecedor` (obrigatório para movimentações de entrada)

## Próximos passos sugeridos
- Autenticação/autorização (JWT + `usuario`/`cargo`)
- Módulo de Vendas (`venda`, `produtoVenda`, `cliente`)
- Testes automatizados (JUnit + Mockito)
