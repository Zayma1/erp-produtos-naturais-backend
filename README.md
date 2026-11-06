# ERP Vila Verde — recebimento persistente

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

Todas as APIs de negócio exigem sessão; não há cadastro público. `GET /api/auth/csrf` cria/retorna a proteção CSRF; envie o token em `X-CSRF-TOKEN` em login, logout e demais escritas. Endpoints: `POST /api/auth/login` (`email`, `senha`), `GET /api/auth/me`, `POST /api/auth/logout`, `POST /api/auth/alterar-senha` (`senhaAtual`, `novaSenha`). O login normaliza e-mail, usa BCrypt (custo 12), limita cinco falhas em 15 minutos por e-mail no PostgreSQL e devolve 429 durante o bloqueio. Mensagem de credencial inválida é genérica. Senhas têm mínimo de 12 caracteres e máximo de 72 bytes UTF-8.

Sessões Spring Session JDBC usam o cookie `SESSION`, com expiração de 30 minutos de inatividade e 12 horas desde o login. O identificador gira no login. Troca/reset de senha, desativação e qualquer alteração administrativa invalidam sessões por remoção e versão de autenticação persistida. Senhas temporárias permitem apenas consultar identidade/CSRF, trocar senha ou sair. A troca invalida as sessões; entre novamente com a nova senha. Respostas não incluem hashes.

`/api/usuarios` é exclusivo de ADMIN: GET lista, POST cria; `/{id}` GET consulta, PUT edita, DELETE **desativa**, sem apagar referências históricas; POST `/{id}/senha-temporaria` recebe `senha`. POST/PUT recebem nome, e-mail, cargo, status (`ATIVO`/`INATIVO`), lojas (UUIDs) e senha temporária (obrigatória na criação; opcional na edição). Um lock PostgreSQL comum serializa mudanças em contas/bootstrap e impede remoção concorrente do último administrador ativo.

ADMIN gerencia contas/lojas e todos os dados. GER gerencia catálogo compartilhado, clientes, fornecedores e compras/recebimentos em lojas atribuídas. FUNC lê catálogo, cria/consulta/edita clientes e registra vendas abertas; lê estoque e movimentos apenas das lojas atribuídas. Catálogo, clientes e fornecedores são organizacionais. Listagens de recursos associados a lojas restringem o SQL às atribuições; detalhes e gravações revalidam a loja e ocultam IDs indevidos com 404. Operações proibidas recebem 403. Vendas PUT/DELETE, checkout, ajustes, transferências e demais fluxos sem suporte continuam negados. Omissão de filtro não libera lojas. Compras/recebimentos preservam o autor e registram modificador; confirmação/movimentos usam o confirmador real. IDs de operador forjados são rejeitados; replay de idempotência também valida ator e loja.

### Migração e primeiro administrador

V3 preserva IDs e referências; copia cargos antigos para `cargo_legado`, normaliza e-mails, invalida **todas** as senhas anteriores e marca contas como não revisadas. Nenhum cargo antigo concede privilégio ou atribuição automática: o cargo efetivo inicial é FUNC, sem lojas e sem login. Colisões de e-mail normalizado interrompem a migração com os IDs para reconciliação; nunca há fusão de contas. Faça backup e siga o preflight/baseline das migrações anteriores; adicionalmente execute `DATABASE_URL=... bash scripts/auth-preflight.sh` antes de V3. Esse comando é somente leitura. V3 repete a verificação dentro da transação. Não use `ddl-auto=create`.

Depois de migrar, execute o comando local com o mesmo datasource e **sem servidor HTTP**:

```bash
./mvnw -DskipTests package
java -jar target/erp-0.0.1-SNAPSHOT.jar --spring.profiles.active=bootstrap-admin --spring.main.web-application-type=none
```

O terminal solicita nome/e-mail e senha temporária sem eco. Não há senha padrão, segredo em argumento ou endpoint de bootstrap. Se não existir administrador ativo, o comando cria um ou revisa a conta do e-mail informado, preservando seu ID. Havendo administrador ativo, recusa novo bootstrap. Use então as APIs administrativas para revisar/resetar as demais contas. Nenhuma credencial antiga é aceita como fallback.

`SESSION_COOKIE_SECURE` é `true` por padrão; use `false` somente em localhost HTTP de desenvolvimento/testes. A aplicação Next relê esse cookie pelo seu proxy de mesma origem; o cookie público é host-only, HttpOnly, SameSite=Lax, Secure fora de localhost. Nunca exponha a URL de banco ou credenciais no frontend.

### Testes

`bash scripts/test-postgres.sh` cria PostgreSQL descartável, executa preflight real e testa migrações, colisões, invalidação legada, login/hash/CSRF, rotação, expirações, revogações, perfis/lojas, replay/identidade, atribuição e concorrência do último administrador, além da regressão de recebimento. `IntegrationBootstrapTests` só existe no classpath de teste, exige `CATALOG_DISPOSABLE=1`, datasource loopback e senha aleatória fornecida pelo runner; não cria endpoint nem faz parte do JAR executável. O SDK completo é regenerado do contrato `openapi-stage8.json` com o lock Kubb existente.

A implantação deve ser coordenada com o frontend. Esta implementação não executa migrações em bancos compartilhados, publicação ou merge de release.
