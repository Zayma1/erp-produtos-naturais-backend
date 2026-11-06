# SDK TypeScript com Kubb

O contrato atual da etapa 12 está integrado em `novaVersao`, a partir de
`stage12-returns`. O código de referência validado é
`c87fe72a66ca2fe8aaf14bead7101c0e95e27267`; `openapi-stage12.json` foi exportado
com ADMIN autenticado por `ReturnsTests.exportOpenApi`, em PostgreSQL V7
descartável. A API `/v3/api-docs` exige ADMIN.

## Geração reproduzível

Use o contrato versionado e o lockfile existente:

```bash
npm ci
OPENAPI_FILE=openapi-stage12.json npm run sdk:generate
```

`kubb.config.ts` lê `OPENAPI_FILE` e grava o SDK completo em `generated/sdk`,
incluindo `.kubb`. O fallback relativo `/v3/api-docs` não aponta automaticamente
para um servidor publicado. Para outro contrato, informe explicitamente um
arquivo exportado da versão desejada. Não edite arquivos gerados manualmente.

## Consumidor frontend

Copie o SDK completo, incluindo `.kubb`, e mantenha `generated/PROVENANCE.md`
e o pin de `scripts/integration.mjs` alinhados ao contrato validado. O frontend
integrado em `main` usa Axios e TanStack Query e chama o proxy de mesma origem:

```ts
import { client } from './generated/sdk/.kubb/client'

client.setConfig({ baseURL: '' })
```

As URLs geradas já começam em `/api`. A configuração de origem, sessão e CSRF
fica no consumidor; não fixe produção no SDK. Os tipos estão em
`generated/sdk/types`, os clientes em `generated/sdk/clients` e os hooks em
`generated/sdk/hooks`.

O merge de documentação não altera o contrato nem exige nova geração. Nenhum
deploy manual ou migração de banco compartilhado faz parte dessa integração.
