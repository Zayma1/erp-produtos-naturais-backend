#!/usr/bin/env bash
set -euo pipefail
umask 077
: "${DATABASE_URL:?Set a PostgreSQL libpq URL (credentials through PGPASSFILE)}"
: "${BACKUP_PATH:?Set the destination of a new backup file}"
repo=$(cd -- "$(dirname -- "$0")/.." && pwd)
if [[ -e "$BACKUP_PATH" ]]; then echo 'Backup already exists; choose a new file' >&2; exit 1; fi
pg_dump --dbname="$DATABASE_URL" --format=custom --file="$BACKUP_PATH"
pg_restore --list "$BACKUP_PATH" >/dev/null
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT
sed 's/public\./stage7_expected./g' "$repo/src/main/resources/db/migration/V1__legacy_schema.sql" > "$scratch/legacy-schema.sql"
sed -n '1,/END \$\$;/p' "$repo/src/main/resources/db/migration/V2__purchase_receiving.sql" > "$scratch/receiving-guard.sql"
cp "$repo/scripts/preflight.sql" "$scratch/preflight.sql"
(cd "$scratch" && psql --dbname="$DATABASE_URL" -X --single-transaction --file=preflight.sql)
echo 'Backup and read-only preflight passed. Application must remain stopped.'
echo 'Next: explicitly run Flyway baseline at version 1, then migrate using the versioned locations.'
echo 'Never set baselineOnMigrate or use Hibernate create/update on this database.'
