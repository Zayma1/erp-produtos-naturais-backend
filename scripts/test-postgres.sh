#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")/.."
name="vila-receiving-test-$RANDOM-$$"
trap 'docker rm -f "$name" >/dev/null 2>&1 || true' EXIT
docker run -d --rm --name "$name" -p 127.0.0.1::5432 -e POSTGRES_DB=receiving_test -e POSTGRES_USER=stage7 -e POSTGRES_PASSWORD=local-stage7-only postgres:16 >/dev/null
until docker exec "$name" pg_isready -U stage7 -d receiving_test >/dev/null 2>&1; do sleep 1; done
port=$(docker inspect --format '{{(index (index .NetworkSettings.Ports "5432/tcp") 0).HostPort}}' "$name")
# Validate the actual backup/schema/data preflight on an isolated legacy database.
docker exec "$name" createdb -U stage7 legacy_preflight
docker exec -i "$name" psql -U stage7 -d legacy_preflight -v ON_ERROR_STOP=1 < src/main/resources/db/migration/V1__legacy_schema.sql >/dev/null
docker exec "$name" mkdir -p /tmp/check/src/main/resources/db
docker cp scripts "$name":/tmp/check/scripts >/dev/null
docker cp src/main/resources/db/migration "$name":/tmp/check/src/main/resources/db/migration >/dev/null
docker exec -e DATABASE_URL=postgresql://stage7@localhost/legacy_preflight -e PGPASSWORD=local-stage7-only -e BACKUP_PATH=/tmp/legacy.dump "$name" bash /tmp/check/scripts/migrate-existing.sh
docker exec "$name" psql -U stage7 -d legacy_preflight -c 'ALTER TABLE item_compra ALTER COLUMN quantidade TYPE numeric(15,3)' >/dev/null
if docker exec -e DATABASE_URL=postgresql://stage7@localhost/legacy_preflight -e PGPASSWORD=local-stage7-only -e BACKUP_PATH=/tmp/incompatible.dump "$name" bash /tmp/check/scripts/migrate-existing.sh; then
  echo 'ERROR: incompatible schema passed preflight' >&2; exit 1
fi
TEST_DATABASE_URL="jdbc:postgresql://127.0.0.1:$port/receiving_test" bash ./mvnw --batch-mode --no-transfer-progress test
