#!/bin/sh
# Runs once, on first init of the postgres data volume (postgres image convention:
# any script in /docker-entrypoint-initdb.d/ executes against POSTGRES_DB/POSTGRES_USER).
# Creates the second database (keycloak) in the same instance - see design.md Decision 3.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE keycloak OWNER $POSTGRES_USER;
EOSQL
