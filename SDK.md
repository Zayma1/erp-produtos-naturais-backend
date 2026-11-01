# SDK TypeScript com Kubb

O SDK é gerado a partir do contrato OpenAPI exposto pelo Springdoc em `/v3/api-docs`.

## Gerar localmente

Com o backend rodando:

```bash
npm install
npm run sdk:generate
```

Por padrão, o Kubb consulta o contrato publicado em:
`https://erp-produtos-naturais-backend.onrender.com/v3/api-docs`.

Esse endereço está configurado em `kubb.config.ts`, e os arquivos são gravados
em `generated/sdk`.

O frontend deve instalar `axios` e `@tanstack/react-query`. Depois de copiar ou publicar `generated/sdk`, configure a URL da API antes de usar o cliente:

```ts
import { client } from './generated/sdk/.kubb/client'

client.setConfig({
  baseURL: 'https://erp-produtos-naturais-backend.onrender.com',
})
```

Os tipos ficam em `generated/sdk/types`, os clientes Axios em `generated/sdk/clients` e os hooks TanStack Query em `generated/sdk/hooks`.
