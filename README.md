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
