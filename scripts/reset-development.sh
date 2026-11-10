#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")/.."
name=vila-verde-stage13-development
if docker inspect "$name" >/dev/null 2>&1; then
  owner=$(docker inspect --format '{{index .Config.Labels "vila-verde.owner"}}' "$name")
  [[ "$owner" == stage13-development ]] || { echo 'Recusado: container sem identificação de desenvolvimento desta etapa.' >&2; exit 1; }
  docker rm -f "$name" >/dev/null
fi
password=$(openssl rand -hex 24)
# Disposable data resides only in this labelled container; no external volumes or endpoints.
docker run -d --name "$name" --label vila-verde.owner=stage13-development -p 127.0.0.1::5432 -e POSTGRES_DB=catalog -e POSTGRES_USER=catalog -e POSTGRES_PASSWORD="$password" postgres:16 >/dev/null
port=$(docker inspect --format '{{(index (index .NetworkSettings.Ports "5432/tcp") 0).HostPort}}' "$name")
for attempt in {1..60}; do docker exec "$name" pg_isready -U catalog -d catalog >/dev/null 2>&1 && break; sleep 1; done
# Existing bootstrap is test-only, guarded by disposable flag and loopback URL.
admin_password=$(openssl rand -hex 24)
DB_URL="jdbc:postgresql://127.0.0.1:$port/catalog" DB_USER=catalog DB_PASSWORD="$password" CATALOG_DISPOSABLE=1 INTEGRATION_ADMIN_PASSWORD="$admin_password" bash ./mvnw --batch-mode --no-transfer-progress test -Dtest=IntegrationBootstrapTests
printf 'Base de desenvolvimento recriada e inicializada: container %s, porta loopback %s. Nenhuma configuração publicada foi alterada.\n' "$name" "$port"
# A private local file supplies dev API credentials without editing repository/published configuration.
umask 077
printf 'export DB_URL=%q\nexport DB_USER=catalog\nexport DB_PASSWORD=%q\nexport SESSION_COOKIE_SECURE=false\nexport INTEGRATION_ADMIN_PASSWORD=%q\n' "jdbc:postgresql://127.0.0.1:$port/catalog" "$password" "$admin_password" > /tmp/vila-verde-stage13-development.env
printf 'Credenciais locais: /tmp/vila-verde-stage13-development.env (permissão 600).\n' 
