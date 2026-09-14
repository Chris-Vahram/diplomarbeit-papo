#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GATEWAY="$SCRIPT_DIR/papo-deploy-gateway"
TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/papo-gateway-test.XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT HUP INT TERM
SOURCE="$TEST_ROOT/source" ORIGIN="$TEST_ROOT/origin.git" STATE="$TEST_ROOT/state"
DEPLOY="$TEST_ROOT/deploy" STUBS="$TEST_ROOT/stubs" LOG="$TEST_ROOT/docker.log"
mkdir -p "$SOURCE" "$STATE/incoming" "$STATE/ssh" "$DEPLOY/frontend-live/releases" "$STUBS"
: > "$LOG"

for stub in flock runuser docker; do :; done
printf '%s\n' '#!/bin/sh' '[ "${PAPO_TEST_LOCK_FAIL-}" != 1 ]' > "$STUBS/flock"
printf '%s\n' '#!/bin/sh' '[ "$1" = --user ] && shift 2; [ "$1" = -- ] && shift; exec "$@"' > "$STUBS/runuser"
printf '%s\n' '#!/bin/sh' 'printf "%s\n" "$*" >> "$PAPO_TEST_LOG"; case "$*" in *"${PAPO_TEST_DOCKER_FAIL_MATCH:?}"*) exit 1;; esac' > "$STUBS/docker"
chmod 700 "$STUBS"/*
export PATH="$STUBS:$PATH" PAPO_DEPLOY_TESTING=1 PAPO_TEST_ROOT="$TEST_ROOT" PAPO_TEST_ORIGIN="$ORIGIN"
export PAPO_TEST_LOG="$LOG" PAPO_TEST_DOCKER_FAIL_MATCH='__never__'

git -C "$SOURCE" init -q
git -C "$SOURCE" config user.name Test
git -C "$SOURCE" config user.email test@example.invalid
mkdir -p "$SOURCE/frontend"
printf '%s\n' '<!doctype html>' > "$SOURCE/frontend/index.html"
git -C "$SOURCE" add frontend && git -C "$SOURCE" commit -qm initial
git init -q --bare "$ORIGIN"
git -C "$SOURCE" remote add origin "$ORIGIN"
git -C "$SOURCE" branch -M main
git -C "$SOURCE" push -q -u origin main
git clone -q --bare "$ORIGIN" "$STATE/repository.git"
printf '%s\n' 'DOMAIN=example.invalid' > "$DEPLOY/.env.prod"
chmod 600 "$DEPLOY/.env.prod"

pass=0 failures=0
ok() { pass=$((pass + 1)); }
bad() { printf 'FEHLER: %s\n' "$1" >&2; failures=$((failures + 1)); }
run_ok() { label=$1 command=$2 input=${3-}; if [ -n "$input" ]; then SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" < "$input" >/dev/null 2>&1 && ok || bad "$label"; else SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1 && ok || bad "$label"; fi; }
run_fail() { label=$1 command=$2 input=${3-}; before=$(wc -l < "$LOG" | tr -d ' '); if [ -n "$input" ]; then SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" < "$input" >/dev/null 2>&1 && bad "$label" || ok; else SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1 && bad "$label" || ok; fi; [ "$before" = "$(wc -l < "$LOG" | tr -d ' ')" ] || bad "$label führte Docker aus"; }
archive_for() { git -C "$SOURCE" archive --format=tar --output="$2" "$1" -- frontend; }
publish() { printf '%s\n' "$1" >> "$SOURCE/frontend/index.html"; git -C "$SOURCE" add frontend; git -C "$SOURCE" commit -qm "$1"; git -C "$SOURCE" push -q origin main; }

sha=$(git -C "$SOURCE" rev-parse HEAD); archive="$TEST_ROOT/frontend.tar"; archive_for "$sha" "$archive"
run_fail 'leerer Befehl' ''
run_fail 'Shell' "shell $sha"
run_fail 'kurze SHA' 'verify abc'
run_fail 'zusätzliches Argument' "verify $sha extra"
run_fail 'uppercase SHA' "verify $(printf %s "$sha" | tr a-f A-F)"
printf x > "$TEST_ROOT/bad.tar"; run_fail 'manipuliertes Archiv' "upload $sha" "$TEST_ROOT/bad.tar"
dd if=/dev/zero of="$TEST_ROOT/large.tar" bs=1048576 count=25 2>/dev/null; printf x >> "$TEST_ROOT/large.tar"
run_fail 'übergroßes Archiv' "upload $sha" "$TEST_ROOT/large.tar"
PAPO_TEST_LOCK_FAIL=1; export PAPO_TEST_LOCK_FAIL; run_fail 'Lock' "upload $sha" "$archive"; unset PAPO_TEST_LOCK_FAIL
run_ok 'Upload' "upload $sha" "$archive"
before_release=$(find "$DEPLOY/frontend-live/releases" -mindepth 1 | wc -l); before_docker=$(wc -l < "$LOG")
run_ok 'Verify' "verify $sha"
[ "$before_release" = "$(find "$DEPLOY/frontend-live/releases" -mindepth 1 | wc -l)" ] || bad 'Verify erzeugte Release'
[ "$before_docker" = "$(wc -l < "$LOG")" ] || bad 'Verify führte Docker aus'

publish newer; old_sha=$sha; sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$sha" "$archive"
run_fail 'veraltete SHA' "upload $old_sha" "$TEST_ROOT/frontend.tar"
ln -s index.html "$SOURCE/frontend/link"; git -C "$SOURCE" add frontend/link; git -C "$SOURCE" commit -qm symlink; git -C "$SOURCE" push -q origin main
link_sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$link_sha" "$archive"
run_fail 'Symlink im Archiv' "upload $link_sha" "$archive"
git -C "$SOURCE" rm -q frontend/link; git -C "$SOURCE" commit -qm regular; git -C "$SOURCE" push -q origin main
sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$sha" "$archive"; run_ok 'Upload zur Aktivierung' "upload $sha" "$archive"

: > "$LOG"; run_ok 'Aktivierung' "activate $sha"
[ "$(readlink "$DEPLOY/frontend-live/current")" = "releases/$sha" ] || bad 'current-Ziel falsch'
[ -f "$DEPLOY/frontend-live/releases/$sha/index.html" ] || bad 'Frontend nicht abgeflacht'
[ "$(sed -n 1p "$LOG")" = "compose -f docker-compose.prod.yml --env-file $DEPLOY/.env.prod pull backend" ] || bad 'pull nicht backend-only'
[ "$(sed -n 2p "$LOG")" = "compose -f docker-compose.prod.yml --env-file $DEPLOY/.env.prod up -d --no-deps backend" ] || bad 'up nicht backend-only'

old_target=$(readlink "$DEPLOY/frontend-live/current"); publish fail; failed_sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$failed_sha" "$archive"; run_ok 'Fehler-Upload' "upload $failed_sha" "$archive"
PAPO_TEST_DOCKER_FAIL_MATCH='pull backend'; export PAPO_TEST_DOCKER_FAIL_MATCH
SSH_ORIGINAL_COMMAND="activate $failed_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Pull-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Pull-Fehler änderte current'
PAPO_TEST_DOCKER_FAIL_MATCH='up -d --no-deps backend'; export PAPO_TEST_DOCKER_FAIL_MATCH
SSH_ORIGINAL_COMMAND="activate $failed_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Compose-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Compose-Fehler änderte current'

chmod 644 "$DEPLOY/.env.prod"
run_fail 'unsichere env-Datei' "activate $failed_sha"

if [ "$failures" -ne 0 ]; then printf '%s\n' "$failures Harness-Prüfung(en) fehlgeschlagen" >&2; exit 1; fi
printf '%s\n' "$pass Harness-Prüfungen bestanden"
