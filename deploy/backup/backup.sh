#!/bin/sh
# Private local and encrypted-offsite database backup (design.md Decision 7).
# Versioned scripts may live in an immutable release, while configuration, dumps and logs
# remain below ~/papo-deploy so a release switch cannot remove them.
#
# Install on the VM via cron, e.g. (see docs/DEPLOYMENT.md):
#   0 3 * * * /home/papo/papo-deploy/current/deploy/backup/backup.sh >> /home/papo/papo-deploy/backups/backup.log 2>&1
set -eu
umask 077

RETENTION_COUNT=14
DEPLOY_ROOT="${PAPO_DEPLOY_ROOT:-$HOME/papo-deploy}"
ENV_FILE="$DEPLOY_ROOT/.env.prod"
BACKUP_DIR="$DEPLOY_ROOT/backups"

case "$DEPLOY_ROOT" in
	""|/) echo "Unsicheres Deployment-Verzeichnis" >&2; exit 1 ;;
esac

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"

# shellcheck disable=SC1091
. "$ENV_FILE"
: "${DB_USER:?DB_USER fehlt in .env.prod}"
: "${BACKUP_RCLONE_DEST:?BACKUP_RCLONE_DEST fehlt in .env.prod}"

STAMP="$(date +%Y%m%d-%H%M%S)"

for DB in partnerportal keycloak; do
	TMP_FILE="$BACKUP_DIR/.${DB}-${STAMP}.dump.tmp"
	DUMP_FILE="$BACKUP_DIR/${DB}-${STAMP}.dump"
	docker exec papo-prod-postgres pg_dump -U "$DB_USER" -d "$DB" \
		> "$TMP_FILE"
	chmod 600 "$TMP_FILE"
	mv "$TMP_FILE" "$DUMP_FILE"
	rclone copyto "$DUMP_FILE" "${BACKUP_RCLONE_DEST%/}/$(basename "$DUMP_FILE")"
done

# Retention: keep the last $RETENTION_COUNT dumps per database, prune the rest.
for DB in partnerportal keycloak; do
	# shellcheck disable=SC2012
	ls -1t "$BACKUP_DIR/${DB}"-*.dump 2>/dev/null \
		| tail -n "+$((RETENTION_COUNT + 1))" \
		| xargs -r rm -f
done
