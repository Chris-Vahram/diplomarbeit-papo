#!/bin/sh
# Renders the realm import file from the template (substituting the real DOMAIN,
# see design.md Decision 4 status note) before Keycloak reads it, then starts
# Keycloak normally. Runs on every container start - always in sync with DOMAIN,
# nothing hardcoded/stale.
set -e

# /opt/keycloak/data/import doesn't exist in the base image - Docker would auto-create it
# as root (unwritable by the keycloak user) if a file were bind-mounted directly into it,
# so the template is mounted elsewhere and this script creates the dir itself, inheriting
# write access from the already group-writable /opt/keycloak/data (gid 0, keycloak is in it).
mkdir -p /opt/keycloak/data/import

sed "s#__DOMAIN__#${DOMAIN}#g" \
	/opt/keycloak/partnerportal-realm.json.template \
	> /opt/keycloak/data/import/partnerportal-realm.json

# End-customer realm papo-shop (add-shop-customer-accounts). Imported only when the backend's
# admin client secret is set: an unset ${SHOP_ADMIN_CLIENT_SECRET} placeholder would otherwise
# stay as a guessable literal secret. Keycloak resolves the ${...} placeholders from this
# container's environment at import; sed only fills in the domain.
#
# Keycloak pastes those values into the JSON text unescaped: a '"' or '\' in an SMTP password
# would break the file and stop Keycloak from starting at all (portal login included; found in
# the local import test 02.11.2026). The template therefore only reads the *_JSON variants,
# escaped here for a JSON string; tabs and line breaks are dropped.
json_escape() {
	printf '%s' "$1" | tr -d '\r\n\t' | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'
}
SMTP_HOST_JSON=$(json_escape "${SMTP_HOST:-localhost}")
SMTP_PORT_JSON=$(json_escape "${SMTP_PORT:-587}")
SMTP_USERNAME_JSON=$(json_escape "${SMTP_USERNAME:-}")
SMTP_PASSWORD_JSON=$(json_escape "${SMTP_PASSWORD:-}")
SHOP_SMTP_SSL_JSON=$(json_escape "${SHOP_SMTP_SSL:-false}")
SHOP_SMTP_STARTTLS_JSON=$(json_escape "${SHOP_SMTP_STARTTLS:-true}")
SHOP_ADMIN_CLIENT_SECRET_JSON=$(json_escape "${SHOP_ADMIN_CLIENT_SECRET:-}")
export SMTP_HOST_JSON SMTP_PORT_JSON SMTP_USERNAME_JSON SMTP_PASSWORD_JSON \
	SHOP_SMTP_SSL_JSON SHOP_SMTP_STARTTLS_JSON SHOP_ADMIN_CLIENT_SECRET_JSON

if [ "${#SHOP_ADMIN_CLIENT_SECRET}" -ge 32 ]; then
	sed "s#__DOMAIN__#${DOMAIN}#g" \
		/opt/keycloak/papo-shop-realm.json.template \
		> /opt/keycloak/data/import/papo-shop-realm.json
else
	rm -f /opt/keycloak/data/import/papo-shop-realm.json
	echo "papo-shop realm not imported: SHOP_ADMIN_CLIENT_SECRET missing or shorter than 32 characters" >&2
fi

exec /opt/keycloak/bin/kc.sh start --import-realm
