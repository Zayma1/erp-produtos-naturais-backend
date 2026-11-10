import { defineConfig } from 'kubb/config'
import { pluginAxios } from '@kubb/plugin-axios'
import { pluginReactQuery } from '@kubb/plugin-react-query'
import { pluginTs } from '@kubb/plugin-ts'

const apiUrl = ''
const openApiUrl = `${apiUrl}/v3/api-docs`

export default defineConfig({
  name: 'erp-produtos-naturais-sdk',
  input: process.env.OPENAPI_FILE || openApiUrl,
  output: {
    path: './generated/sdk',
    clean: true,
  },
  plugins: [
    pluginTs({
      output: {
        path: 'types',
        barrel: { type: 'named' },
      },
    }),
    pluginAxios({
      baseURL: apiUrl,
      output: {
        path: 'clients',
        barrel: { type: 'named' },
      },
    }),
    pluginReactQuery({
      client: 'axios',
      hooks: true,
      output: {
        path: 'hooks',
        barrel: { type: 'named' },
      },
    }),
  ],
})
