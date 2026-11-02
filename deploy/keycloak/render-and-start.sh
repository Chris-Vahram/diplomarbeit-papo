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
# stay as a guessable literal secret. Keycloak resolves the ${...} SMTP/secret placeholders
# from this container's environment at import; sed only fills in the domain.
if [ "${#SHOP_ADMIN_CLIENT_SECRET}" -ge 32 ]; then
	sed "s#__DOMAIN__#${DOMAIN}#g" \
		/opt/keycloak/papo-shop-realm.json.template \
		> /opt/keycloak/data/import/papo-shop-realm.json
else
	rm -f /opt/keycloak/data/import/papo-shop-realm.json
	echo "papo-shop realm not imported: SHOP_ADMIN_CLIENT_SECRET missing or shorter than 32 characters" >&2
fi

exec /opt/keycloak/bin/kc.sh start --import-realm
