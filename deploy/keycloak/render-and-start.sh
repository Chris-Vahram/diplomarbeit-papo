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

exec /opt/keycloak/bin/kc.sh start --import-realm
