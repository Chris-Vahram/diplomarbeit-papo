#!/bin/sh
# Runs once, on first container start (mounted into /docker-entrypoint-initdb.d/), while the
# official postgres image's entrypoint is already connected as $POSTGRES_USER against
# $POSTGRES_DB (partnerportal). Adds the second database Keycloak needs, owned by the same
# role - see design.md Decision 3 (shared instance, separate databases, single role).
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE keycloak OWNER $POSTGRES_USER;
EOSQL
