#!/usr/bin/env bash
set -euo pipefail
: "${DATABASE_URL:?Informe DATABASE_URL PostgreSQL para leitura}"
report=$(psql "$DATABASE_URL" -X -A -t -v ON_ERROR_STOP=1 -c "SELECT lower(btrim(email)) || ': ' || string_agg(id::text, ', ' ORDER BY id) FROM usuario GROUP BY lower(btrim(email)) HAVING count(*)>1")
if [[ -n "$report" ]]; then
  printf 'Reconcilie e-mails normalizados antes de V3; nenhuma conta foi alterada:\n%s\n' "$report" >&2
  exit 1
fi
printf 'Nenhuma colisão de e-mail normalizado. V3 invalidará todas as credenciais legadas.\n'
