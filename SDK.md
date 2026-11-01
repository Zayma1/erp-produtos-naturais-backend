# SDK TypeScript com Kubb

O SDK é gerado a partir do contrato OpenAPI exposto pelo Springdoc em `/v3/api-docs`.

## Gerar localmente

Com o backend rodando:

```bash
npm install
npm run sdk:generate
```

Por padrão, o Kubb consulta `http://localhost:8080/v3/api-docs` e grava os arquivos em `generated/sdk`.

Para usar outro ambiente:

```bash
OPENAPI_URL=https://api.exemplo.com/v3/api-docs npm run sdk:generate
```

No Windows PowerShell:

```powershell
$env:OPENAPI_URL = "https://api.exemplo.com/v3/api-docs"
npm run sdk:generate
```

O frontend deve instalar `axios` e `@tanstack/react-query`. Depois de copiar ou publicar `generated/sdk`, configure a URL da API antes de usar o cliente:

```ts
import { client } from './generated/sdk/.kubb/client'

client.setConfig({
  baseURL: import.meta.env.VITE_API_URL,
})
```

Os tipos ficam em `generated/sdk/types`, os clientes Axios em `generated/sdk/clients` e os hooks TanStack Query em `generated/sdk/hooks`.
