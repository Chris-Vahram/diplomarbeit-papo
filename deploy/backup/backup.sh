#!/bin/sh
# Local pg_dump backup for both production databases (design.md Decision 7, local half).
# Dumps land next to this script (deploy/backup/*.dump - see .gitignore) and are pruned
# to the last 14 per database. Offsite sync (rclone, task 7.2/7.3) is deferred post-MVP
# and not wired in here yet.
#
# Install on the VM via cron, e.g. (see docs/DEPLOYMENT.md):
#   0 3 * * * /home/papo/papo-deploy/deploy/backup/backup.sh >> /home/papo/papo-deploy/deploy/backup/backup.log 2>&1
set -eu

RETENTION_COUNT=14
BACKUP_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_DIR="$(cd "$BACKUP_DIR/../.." && pwd)"

# Read only DB_USER instead of sourcing .env.prod: secrets such as SMTP_PASSWORD may contain
# shell metacharacters that docker compose accepts but sh cannot parse (and the script needs
# no other variable).
DB_USER="$(grep '^DB_USER=' "$COMPOSE_DIR/.env.prod" | head -n 1 | cut -d= -f2-)"
if [ -z "$DB_USER" ]; then
	echo "DB_USER fehlt in $COMPOSE_DIR/.env.prod" >&2
	exit 1
fi

STAMP="$(date +%Y%m%d-%H%M%S)"

for DB in partnerportal keycloak; do
	docker exec papo-prod-postgres pg_dump -U "$DB_USER" -d "$DB" \
		> "$BACKUP_DIR/${DB}-${STAMP}.dump"
done

# Retention: keep the last $RETENTION_COUNT dumps per database, prune the rest.
for DB in partnerportal keycloak; do
	# shellcheck disable=SC2012
	ls -1t "$BACKUP_DIR/${DB}"-*.dump 2>/dev/null \
		| tail -n "+$((RETENTION_COUNT + 1))" \
		| xargs -r rm -f
done
