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

# shellcheck disable=SC1091
. "$COMPOSE_DIR/.env.prod"

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
