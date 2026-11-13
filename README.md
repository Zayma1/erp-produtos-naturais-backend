Etapa 14 integrada à `novaVersao`, com histórico e branches preservados. [Publicação e CI aprovadas](docs/STAGE14_RELEASE.md).

Etapa 14 (`stage14-forms-auth`): login por campo `usuario` real ou e-mail, recuperação SMTP com CSRF, validação de documentos e localidades IBGE. Consulte [configuração da etapa 14](docs/STAGE14_FORMS_AUTH.md). V1–V8 preservadas; V9 Java faz preflight e registra o mapa de usuários em `usuario_login_migracao`.

# ERP Vila Verde — recebimento persistente

## Etapa 13 — variante comercial obrigatória

Branch `stage13-sku-variants`, base `novaVersao` em `f38a335774b5f4b998514109e543932ed2cc8fe6`. As descrições anteriores das etapas 7–12 abaixo são históricas. Produto contém informações gerais; SKU exige apresentação, unidade e tipo de venda. Todos os itens comerciais usam `idProdutoSku`, com `idProduto` derivado nas respostas. O estoque é único por SKU + loja; todos os escritores usam ordem SKU-ID → loja-ID.

Cadastro: `/api/produtos-sku`. Vínculos: `/api/skus-loja`. Preços: `/api/skus-precos-loja`; as rotas por produto foram removidas da segurança e dos controllers. Consultas de estoque/movimentos e vínculos/preços combinam `idProduto`, `idProdutoSku` e `idLoja`; `/api/estoques/saldo` exige `idProdutoSku` + `idLoja`.

Código/apresentação/unidade são snapshots nos documentos. Recebimentos e devoluções herdam o SKU e os snapshots da origem. UNIDADE exige inteiros e PESO admite até três casas; o servidor valida também recebimentos/devoluções e o banco aplica a proteção de quantidade/snapshot. SKU utilizado não pode mudar produto/unidade/tipo nem ser excluído: use inativação. Documentos existentes e devoluções de SKU inativo continuam operacionais conforme suas permissões.

V1–V7 são imutáveis; V8 recusa dados comerciais antigos e solicita reset exclusivo de desenvolvimento. Execute `bash scripts/reset-development.sh` para recriar somente o container local identificado `vila-verde-stage13-development`, sem volumes ou endpoints externos; migrations e bootstrap existentes são aplicados. Credenciais locais ficam em `/tmp/vila-verde-stage13-development.env` com permissão 600. Não execute esse reset sobre ambientes compartilhados. Nenhuma configuração publicada é alterada.

Valide com `bash scripts/test-postgres.sh`. `ReturnsTests` cobre variantes unitárias/granel, falta isolada, snapshots, herança, inativação e concorrência; `MigrationTests` cobre instalação vazia, reconexão e recusa V7 preenchida, além dos testes históricos V1–V7. Exporte OpenAPI autenticado por `ReturnsTests.exportOpenApi`; gere o SDK integral com `OPENAPI_FILE=openapi-stage13.json npm run sdk:generate`, usando o lock existente. Não editar os arquivos gerados.

Sem conversão de embalagens, atributos combinatórios, preço automático, merge ou deploy.


Java 21, PostgreSQL 16. Configure `DB_URL` (JDBC PostgreSQL), `DB_USER`, `DB_PASSWORD` e execute `bash mvnw spring-boot:run`. O perfil prod também aceita `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. Todas as configurações normais usam Hibernate `validate` e Flyway com baseline automático desabilitado. Não existe recriação de tabelas no boot. Use banco vazio para começar; não aponte testes para um banco compartilhado.

`bash scripts/test-postgres.sh` cria e remove seu próprio PostgreSQL Docker, testa backup/preflight, migrações, API e concorrência. O workflow Backend PostgreSQL executa o mesmo comando. A única senha fixa no teste pertence ao container descartável.

## Banco existente

1. Pare os escritores e mantenha o backup fora do repositório. Instale clientes PostgreSQL 16 e Flyway CLI 12.4.0 com suporte PostgreSQL. Use `PGPASSFILE` protegido, sem credenciais no histórico.
2. Configure `DATABASE_URL` (libpq, sem senha) e um `BACKUP_PATH` novo. Execute `bash scripts/migrate-existing.sh`. Ele faz backup custom, verifica o catálogo do backup e compara colunas, tipos, nulabilidade e constraints contra V1 em transação revertida. Teste a restauração desse backup em outro banco antes de prosseguir.
3. A mesma preflight de V2 bloqueia recebimentos históricos concluídos, quantidades já recebidas, compras parcial/totalmente recebidas, movimentos ENTRADA_COMPRA, rascunhos inválidos e triggers externos nas tabelas operacionais. Havendo qualquer um deles, faça reconciliação humana de recibos, itens, saldos e movimentos com evidência e plano de migração específico. Não apague histórico para passar pela verificação, não zere quantidades e não reproduza recebimentos automaticamente. Esta versão só adota automaticamente bases legadas sem efeitos históricos de recebimento.
4. Revise diferenças de índices adicionais, funções, permissões, triggers e customizações com o responsável pelo banco. O comparador cobre o esquema de entidades V1; não certifica customizações externas. Triggers operacionais impedem a migração até serem auditados/removidos por um procedimento separado.
5. Depois da aprovação operacional do backup, restauração e preflight, configure `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`, `FLYWAY_LOCATIONS=filesystem:src/main/resources/db/migration`. Execute explicitamente `flyway -baselineVersion=1 baseline`, então `flyway validate` e `flyway migrate`. Não use `baselineOnMigrate`, Hibernate create/update ou repair para esconder divergências. Execute preflight com a aplicação parada imediatamente antes do baseline; mantenha-a parada até terminar a migração.
6. Inicie o backend com validate, confira o histórico Flyway e os dados preservados. Faça o fluxo parcial/final em um banco restaurado de ensaio antes de habilitar escrita no compartilhado.

## API de recebimento

`/api/recebimentos-compras`: GET lista resumos (filtro opcional `idCompra`); GET `/{id}` inclui itens, versão e conclusão. POST exige `Idempotency-Key` UUID e cria somente `RASCUNHO`. Operador existente, compra e ao menos um item são obrigatórios. Quantidades positivas numeric(14,3), itens únicos da compra e produto correspondente. Datas usam offset; data omitida usa o servidor.

PUT `/{id}` substitui itens de rascunho e exige `versao`. IDs mantidos são preservados inclusive quando o cliente omite seu ID; ID de outro recebimento é rejeitado. A compra não pode mudar. DELETE `/{id}?versao=N` apaga rascunho e itens, preservando tombstone da criação. POST `/{id}/concluir` recebe `{ "versao": N }` e confirma explicitamente. Repetir conclusão retorna o documento concluído, independentemente da versão antiga, sem efeitos adicionais. Confirmado é imutável. Erros: ApiError 400 entrada inválida, 404 ausente, 409 versão/estado/saldo/chave conflitantes.

Rascunhos não reservam saldo nem alteram estoque. Confirmação trava compra, recebimento e incrementa estoques ordenados por produto com ON CONFLICT. Atualiza quantidades recebidas, compra parcial/recebida e grava um movimento ENTRADA_COMPRA por item com operador e data do recebimento, atomicamente. A conclusão usa horário do servidor. Custos e totais da compra não mudam. A unique parcial de movimentos impede duplicação. Criação da chave usa advisory lock transacional antes da compra, hash canônico SHA-256 e retenção permanente da identidade/tombstone. POST idêntico retorna o recibo atual; payload divergente ou rascunho apagado retorna 409. O cliente não deve trocar de chave após resultado incerto.

Compra com qualquer recebimento não pode ser editada/excluída; excluir todos os rascunhos desbloqueia uma compra ainda editável. O lock de compra também protege PUT/DELETE de compra contra criação/confirmação. Isso não acrescenta versão otimista à edição geral de compras/vendas.

## Contrato e SDK

Com a API local em execução: `curl -f http://127.0.0.1:8087/v3/api-docs -o openapi-stage7.json`, `npm ci --include=dev`, `OPENAPI_FILE=./openapi-stage7.json npm run sdk:generate`. A geração completa usa URL base vazia; consumidores podem configurá-la. Não editar arquivos gerados manualmente.

Referências: [locks PostgreSQL](https://www.postgresql.org/docs/16/explicit-locking.html). Autenticação, reversão, devolução, ajustes/transferências e fechamento de venda permanecem fora desta entrega.

## Etapa 8 — autenticação por sessão

A base continua sendo `novaVersao` com recebimento. `cargoUsuario` foi inspecionada e adaptada: seus valores `ADMIN`, `GER`, `FUNC` foram mantidos, mas o loader retornava `null`, faltava registro de serviço, a persistência/validação do enum e as authorities estavam incorretas, `findByUuid` era inválido, as rotas continuavam `permitAll` e a senha era gravada em texto puro. A branch também antecede as garantias de recebimento; não foi incorporada integralmente.

Todas as APIs de negócio exigem sessão; não há cadastro público. `GET /api/auth/csrf` cria/retorna a proteção CSRF; envie o token em `X-CSRF-TOKEN` em login, logout e demais escritas. Endpoints: `POST /api/auth/login` (`email`, `senha`), `GET /api/auth/me`, `POST /api/auth/logout`, `POST /api/auth/alterar-senha` (`senhaAtual`, `novaSenha`). O login normaliza e-mail, usa BCrypt (custo 12), limita cinco falhas em 15 minutos por e-mail no PostgreSQL e devolve 429 durante o bloqueio. Mensagem de credencial inválida é genérica. Senhas novas têm mínimo de 8 caracteres e máximo de 72 bytes UTF-8; somente a credencial inicial automática `admin` é uma exceção temporária, sem acesso de negócio antes da troca.

Sessões Spring Session JDBC usam o cookie `SESSION`, com expiração de 30 minutos de inatividade e 12 horas desde o login. O identificador gira no login. Troca/reset de senha, desativação e qualquer alteração administrativa invalidam sessões por remoção e versão de autenticação persistida. Senhas temporárias permitem apenas consultar identidade/CSRF, trocar senha ou sair. A troca invalida as sessões; entre novamente com a nova senha. Respostas não incluem hashes.

`/api/usuarios` é exclusivo de ADMIN: GET lista, POST cria; `/{id}` GET consulta, PUT edita, DELETE **desativa**, sem apagar referências históricas; POST `/{id}/senha-temporaria` recebe `senha`. POST/PUT recebem nome, e-mail, cargo, status (`ATIVO`/`INATIVO`), lojas (UUIDs) e senha temporária (obrigatória na criação; opcional na edição). Um lock PostgreSQL comum serializa mudanças em contas/bootstrap e impede remoção concorrente do último administrador ativo.

ADMIN gerencia contas/lojas e todos os dados. GER gerencia catálogo compartilhado, clientes, fornecedores e compras/recebimentos em lojas atribuídas. FUNC lê catálogo, cria/consulta/edita clientes e registra vendas abertas; lê estoque e movimentos apenas das lojas atribuídas. Catálogo, clientes e fornecedores são organizacionais. Listagens de recursos associados a lojas restringem o SQL às atribuições; detalhes e gravações revalidam a loja e ocultam IDs indevidos com 404. Operações proibidas recebem 403. Vendas PUT/DELETE, checkout, ajustes, transferências e demais fluxos sem suporte continuam negados. Omissão de filtro não libera lojas. Compras/recebimentos preservam o autor e registram modificador; confirmação/movimentos usam o confirmador real. IDs de operador forjados são rejeitados; replay de idempotência também valida ator e loja.

### Migração e primeiro administrador

V3 preserva IDs e referências; copia cargos antigos para `cargo_legado`, normaliza e-mails, invalida **todas** as senhas anteriores e marca contas como não revisadas. Nenhum cargo antigo concede privilégio ou atribuição automática: o cargo efetivo inicial é FUNC, sem lojas e sem login. Colisões de e-mail normalizado interrompem a migração com os IDs para reconciliação; nunca há fusão de contas. Faça backup e siga o preflight/baseline das migrações anteriores; adicionalmente execute `DATABASE_URL=... bash scripts/auth-preflight.sh` antes de V3. Esse comando é somente leitura. V3 repete a verificação dentro da transação. Não use `ddl-auto=create`.

Após migrar, o backend cria automaticamente o primeiro administrador ao iniciar o servidor HTTP se não existir ADMIN ativo/revisado. Não é necessário shell do Render: login **`admin`**, senha temporária **`admin`**. O e-mail interno dessa conta é `admin@vilaverde.local` (também pode ser usado no login). A senha é gravada com BCrypt e deve ser substituída antes de qualquer acesso de negócio. O alias `admin` e esse e-mail usam o mesmo contador de tentativas.

A inicialização usa o mesmo lock PostgreSQL das alterações de contas: réplicas concorrentes criam uma única conta. Se já existir um administrador ativo, não cria outra conta, não altera identidade/senha e não reverte uma senha já substituída após reinício. `BOOTSTRAP_ADMIN_PASSWORD` permite substituir a senha inicial por uma de ao menos 8 caracteres e no máximo 72 bytes UTF-8; `BOOTSTRAP_ADMIN_ENABLED=false` desativa a inicialização automática. Nenhum segredo é impresso nos logs.

O comando interativo continua disponível como alternativa, com o mesmo datasource e **sem servidor HTTP**:

```bash
./mvnw -DskipTests package
java -jar target/erp-0.0.1-SNAPSHOT.jar --spring.profiles.active=bootstrap-admin --spring.main.web-application-type=none
```

O terminal solicita nome/e-mail e senha temporária sem eco. A alternativa interativa não usa senha padrão nem segredo em argumento; não existe endpoint público de bootstrap. Se não existir administrador ativo, o comando cria um ou revisa a conta do e-mail informado, preservando seu ID. Havendo administrador ativo, recusa novo bootstrap. Use então as APIs administrativas para revisar/resetar as demais contas. Nenhuma credencial antiga é aceita como fallback.

`SESSION_COOKIE_SECURE` é `true` por padrão; use `false` somente em localhost HTTP de desenvolvimento/testes. A aplicação Next relê esse cookie pelo seu proxy de mesma origem; o cookie público é host-only, HttpOnly, SameSite=Lax, Secure fora de localhost. Nunca exponha a URL de banco ou credenciais no frontend.

### Testes

`bash scripts/test-postgres.sh` cria PostgreSQL descartável, executa preflight real e testa migrações, colisões, invalidação legada, login/hash/CSRF, rotação, expirações, revogações, perfis/lojas, replay/identidade, atribuição e concorrência do último administrador, além da regressão de recebimento. `IntegrationBootstrapTests` só existe no classpath de teste, exige `CATALOG_DISPOSABLE=1`, datasource loopback e senha aleatória fornecida pelo runner; não cria endpoint nem faz parte do JAR executável. O SDK completo é regenerado do contrato `openapi-stage8.json` com o lock Kubb existente.

A implantação deve ser coordenada com o frontend. Esta implementação não executa migrações em bancos compartilhados, publicação ou merge de release.

### Vendas (V4)

`POST /api/vendas` exige `Idempotency-Key` UUID, cliente, loja e itens. A identidade
pertence à sessão; `idUsuario` opcional só pode identificar a própria conta. Chave,
hash canônico e venda são persistidos na mesma transação, sem expiração automática.
Repetição concorrente retorna a mesma identidade; outro conteúdo gera 409 e outra
conta recebe 404. A leitura do replay também valida o acesso atual à loja da venda.

`PUT /api/vendas/{id}` exige `versao` e venda aberta. IDs de itens existentes devem
ser mantidos; omissões removem itens e IDs novos devem ficar ausentes. Mudar de loja
exige acesso à origem e ao destino. Preços são informados explicitamente; subtotal
arredonda a soma bruta (HALF_UP), descontos são monetários e totais dos itens são
arredondados separadamente. Observação, versão, totais e unidades históricas retornam
nas consultas. Operações de gravação não aceitam `status` no payload.

`POST /api/vendas/{id}/concluir` recebe `{ "versao": 0 }`. Bloqueia a venda e os
saldos em ordem de produto, agrega produtos repetidos, verifica todos os saldos e
aplica débitos atômicos. Cada item origina uma `SAIDA_VENDA` positiva com ator e
horário do servidor. Repetir uma conclusão operacional retorna a venda existente.
A confirmação registra a venda e a movimentação, **sem registrar pagamento**.

`POST /api/vendas/{id}/cancelar` recebe versão e `motivo` não vazio; só permite venda
aberta, preserva o histórico e não altera estoque. Repetir retorna o cancelamento
existente. Concluídas/canceladas são imutáveis; DELETE permanece proibido.
Inválidos retornam 400, registros ausentes/fora do escopo 404, versões/transições/
estoque em conflito 409. ADMIN/GER/FUNC mantêm sessão, CSRF e escopo de lojas.

Antes de aplicar V4 a dados existentes, execute `scripts/sales-preflight.sql` em
modo de leitura e guarde o relatório de reconciliação. Dados monetários inválidos
bloqueiam a migração; IDs, referências e snapshots válidos são preservados.
Concluídas/canceladas preexistentes recebem `legado=true`, sem inventar atores,
horários ou movimentos. Permanecem consultáveis e nunca são reexecutadas. O relatório
identifica conclusões sem movimento verificável para reconciliação humana.

`bash scripts/test-postgres.sh` usa somente bancos PostgreSQL descartáveis e cobre
vendas, migrações V1–V4, autenticação e recebimento, incluindo concorrência e rollback.

### Transferências de estoque — etapa 10 (V5)

Branch `stage10-transfers`, empilhada sobre `stage9-sales` (`65d6585`). V1–V4 permanecem inalteradas. V5 preserva todos os documentos anteriores como `legado=true`, inclusive rascunhos: somente consulta até reconciliação humana, sem inferir saldos, snapshots ou reproduzir conclusão.

`/api/transferencias-estoque` exige sessão ADMIN/GER e acesso às **duas lojas**. GET aceita `idLojaOrigem`, `idLojaDestino`, `status`; GET `/{id}` retorna itens/IDs, unidade salva, versão e auditoria. POST exige `Idempotency-Key` UUID e cria `RASCUNHO`; PUT `/{id}` substitui o rascunho com `versao`, mantendo IDs informados e removendo omitidos. Campos de status são rejeitados. Operador vem da sessão; eventual `idUsuario` só pode apontar para ela. Criação registra chave, hash canônico e ator na mesma transação; replay revalida ator e acesso às duas lojas. Alterar lojas exige acesso aos pares antigo e novo.

POST `/{id}/concluir` recebe `{ "versao": N }`: move imediatamente estoque da origem ao destino, sem reserva, trânsito ou recebimento posterior. Bloqueia documento e pares de saldo em ordem produto→loja; insere saldos ausentes a zero dentro da transação e valida todos os itens e o limite numeric(14,3) antes dos efeitos. Movimentos positivos `TRANSFERENCIA_SAIDA`/`TRANSFERENCIA_ENTRADA` ligam documento, item e confirmador; usam horário do servidor. Unique por item/direção e replay da confirmação impedem duplicação. A data comercial permanece separada.

POST `/{id}/cancelar` recebe versão e `motivo` obrigatório; apenas rascunhos, sem efeitos no estoque. Repetição retorna o cancelamento existente. Confirmados são imutáveis; DELETE é proibido. Erros: 400 entrada/precisão/produto duplicado/lojas iguais; 404 ausente ou fora do escopo; 409 versão/estado/falta/overflow. Falhas revertem saldos novos e existentes, movimentos e estado do documento.

`bash scripts/test-postgres.sh` cobre transferências, concorrência com recebimentos/vendas e direções opostas, rollback, escopo, CSRF, auditoria, identidade de itens, idempotência, migração V4→V5 e regressões. O contrato autenticado está em `openapi-stage10.json`; gere com `OPENAPI_FILE=./openapi-stage10.json npm run sdk:generate` usando o lock existente. Não executar migração compartilhada, reparo histórico ou deploy como parte desta etapa. Ajustes e reversões continuam adiados.

Etapa 11 (`stage11-adjustments`): ajustes quantitativos em `/api/ajustes-estoque`.
Criação exige UUID `Idempotency-Key`; edição, conclusão e cancelamento exigem
`versao`. ADMIN/GER operam apenas lojas autorizadas. Conclusão bloqueia saldos,
valida o documento inteiro e grava movimentos e snapshots na mesma transação.
V6 preserva registros anteriores como históricos somente leitura e interrompe
migração com consulta de reconciliação para itens incompatíveis ou duplicados.
Não existe contagem física, estorno ou permissão para estoque negativo.

## Stage 12 — customer and supplier returns

`stage12-returns` is based on `bdd39f3` and preserves stages 9–11. Flyway V7
adds lifecycle/version audit, actor-owned creation idempotency, item snapshots
and unique movement references. V1–V6 are unchanged. Both return resources
support collection/detail GET, POST draft with UUID Idempotency-Key, PUT with
version, and explicit POST conclude/cancel. Cancellation requires a reason;
DELETE is forbidden. ADMIN/GER are limited to the source document's store.

Customer returns require a completed non-legacy sale. Mixed stock dispositions
are allowed once per source item/disposition, both consuming the sold allowance.
Only restockable quantities create DEVOLUCAO entries. Non-restockable rows
require an observation. Supplier returns require a partially/fully received
purchase and both received allowance and available stock; they create
DEVOLUCAO_FORNECEDOR exits. Neither workflow changes source totals/status or
cumulative receipt quantities, and neither records refunds/credits/payments.

Confirmation locks source, return, then stock in product/store order, rechecks
confirmed totals, validates the whole document and applies all effects in one
transaction. Replay has no duplicate effects. Drafts reserve no quantity.
Confirmed/cancelled records are immutable. Existing returns are read-only
legacy records and block new returns against their source until reviewed
reconciliation establishes allowance; no effects are inferred or replayed.
SQL read functions expose confirmed-return totals and unresolved legacy flags.
Sale units use saved snapshots; purchase units are labeled as the unit at
return creation, never as historical purchase units.

Run `bash scripts/test-postgres.sh` with Java 21 and Docker. ReturnsTests cover
partial/multiple/mixed returns, allowance, stock, identity, version, authorization,
idempotency, concurrency with other stock writers, overflow and rollback.
MigrationTests cover V6→V7 quarantine and reconnect persistence. Export the
full OpenAPI with ReturnsTests.exportOpenApi, then generate the SDK using
`OPENAPI_FILE=openapi-stage12.json npm run sdk:generate`.

Stage 12 was merged from `stage12-returns` into `novaVersao` on 2026-11-06,
at the user's request, preserving the history of stages 9–11. The backend
`main` branch remains unchanged, and the source branch is retained. The validated
runtime commit is `c87fe72a66ca2fe8aaf14bead7101c0e95e27267`:
[PostgreSQL CI passed](https://github.com/Zayma1/erp-produtos-naturais-backend/actions/runs/42284077339),
75 tests recorded (74 executed, one integration-only bootstrap skipped).
The integration changes documentation around that tested code and triggers the
normal destination-branch checks; it does not perform a manual deployment or
shared-database migration.

Refunds, supplier credits,
fiscal documents, exchanges, cross-store returns, quarantine stock, reversal,
time-window policies and dependency upgrades are deferred.

Consulta IBGE: [diagnóstico e configuração da alternativa regional](docs/IBGE_INCIDENT.md). O backend mantém validação/cache e pode usar a ponte autenticada do próprio frontend em São Paulo quando a rota direta falha. `IBGE_RELAY_ORIGIN=disabled` desabilita essa alternativa; em outros ambientes, configure uma origem HTTPS confiável explicitamente.
