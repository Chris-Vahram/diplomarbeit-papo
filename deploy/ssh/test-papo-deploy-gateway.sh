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
printf '%s\n' '#!/bin/sh' \
    'if [ "$1" = inspect ]; then' \
    '  [ -f "$PAPO_TEST_IMAGE_FILE" ] || exit 1' \
    '  image=$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")' \
    '  [ -z "${PAPO_TEST_INSPECT_IMAGE_OVERRIDE-}" ] || image=$PAPO_TEST_INSPECT_IMAGE_OVERRIDE' \
    '  case "$3" in *RestartCount*) id=test-container; restarts=0; if [ "${PAPO_TEST_INSPECT_CHURN-}" = 1 ]; then n=0; [ ! -f "$PAPO_TEST_INSPECT_COUNT_FILE" ] || n=$(sed -n 1p "$PAPO_TEST_INSPECT_COUNT_FILE"); n=$((n + 1)); printf "%s\n" "$n" > "$PAPO_TEST_INSPECT_COUNT_FILE"; id=test-container-$n; fi; printf "%s|%s|%s|true\n" "$id" "$image" "$restarts" ;; *) printf "%s\n" "$image" ;; esac' \
    '  exit 0' \
    'fi' \
    'printf "%s\n" "$*" >> "$PAPO_TEST_LOG"' \
    'case "$*" in *"${PAPO_TEST_DOCKER_FAIL_MATCH:?}"*) exit 1;; esac' \
    'case "$*" in *"up -d --no-deps backend"*) [ "${PAPO_TEST_FAIL_UP_TAG-}" != "$IMAGE_TAG" ] || exit 1; if [ "${PAPO_TEST_SIGNAL_ON_RESTORE-}" = 1 ] && [ "$IMAGE_TAG" = "${PAPO_TEST_RESTORE_TAG-}" ]; then if [ ! -f "$PAPO_TEST_RESTORE_SIGNAL_FILE" ]; then : > "$PAPO_TEST_RESTORE_SIGNAL_FILE"; kill -TERM "$PPID"; exit 1; elif [ "${PAPO_TEST_SIGNAL_TWICE_ON_RESTORE-}" = 1 ] && [ ! -f "$PAPO_TEST_RESTORE_SECOND_SIGNAL_FILE" ]; then : > "$PAPO_TEST_RESTORE_SECOND_SIGNAL_FILE"; kill -TERM "$PPID"; fi; fi; printf "%s\n" "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$IMAGE_TAG" > "$PAPO_TEST_IMAGE_FILE"; if [ "${PAPO_TEST_SIGNAL_ON_UP-}" = 1 ] && [ ! -f "$PAPO_TEST_SIGNAL_FILE" ]; then : > "$PAPO_TEST_SIGNAL_FILE"; kill -TERM "$PPID"; fi ;; esac' > "$STUBS/docker"
printf '%s\n' '#!/bin/sh' \
    'case "$*" in -q\ *--noproxy*--resolve*https://*) ;; *) exit 8 ;; esac' \
    'case "$*" in *--insecure*) exit 9 ;; esac' \
    'count=0; [ ! -f "$PAPO_TEST_CURL_COUNT_FILE" ] || count=$(sed -n 1p "$PAPO_TEST_CURL_COUNT_FILE")' \
    'count=$((count + 1)); printf "%s\n" "$count" > "$PAPO_TEST_CURL_COUNT_FILE"' \
    '[ "$count" -gt "${PAPO_TEST_CURL_READY_AFTER:-0}" ] || exit 7' \
    'case "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" in *"${PAPO_TEST_CURL_FAIL_IMAGE_TAG:-__never__}") printf 503; exit 0 ;; esac' \
    'printf "%s" "${PAPO_TEST_CURL_STATUS:-401}"' > "$STUBS/curl"
printf '%s\n' '#!/bin/sh' \
    'case "$*" in *"/frontend-live/current"*) [ "${PAPO_TEST_LINK_FAIL-}" != 1 ] || exit 1 ;; esac' \
    'exec /bin/mv "$@"' > "$STUBS/mv"
chmod 700 "$STUBS"/*
export PATH="$STUBS:$PATH" PAPO_DEPLOY_TESTING=1 PAPO_TEST_ROOT="$TEST_ROOT" PAPO_TEST_ORIGIN="$ORIGIN"
export PAPO_TEST_LOG="$LOG" PAPO_TEST_DOCKER_FAIL_MATCH='__never__'
export PAPO_TEST_IMAGE_FILE="$TEST_ROOT/backend-image" PAPO_TEST_CURL_COUNT_FILE="$TEST_ROOT/curl-count"
export PAPO_TEST_INSPECT_COUNT_FILE="$TEST_ROOT/inspect-count"
export PAPO_TEST_SIGNAL_FILE="$TEST_ROOT/signal-sent"
export PAPO_TEST_RESTORE_SIGNAL_FILE="$TEST_ROOT/rollback-signal-sent"
export PAPO_TEST_RESTORE_SECOND_SIGNAL_FILE="$TEST_ROOT/rollback-second-signal-sent"

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
run_fragmented_ok() { label=$1 command=$2 input=$3; split -b 4096 "$input" "$TEST_ROOT/chunk."; if (for chunk in "$TEST_ROOT"/chunk.*; do cat "$chunk"; sleep 0.001; done) | SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1; then ok; else bad "$label"; fi; }
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
run_fragmented_ok 'fragmentierter Upload' "upload $sha" "$archive"
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
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$sha" ] || bad 'Aktivierung zog falsches Image'
[ "$(sed -n 1p "$PAPO_TEST_CURL_COUNT_FILE")" -ge 2 ] || bad 'Backend nicht zweifach geprüft'

old_target=$(readlink "$DEPLOY/frontend-live/current"); publish fail; failed_sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$failed_sha" "$archive"; run_ok 'Fehler-Upload' "upload $failed_sha" "$archive"
PAPO_TEST_DOCKER_FAIL_MATCH='pull backend'; export PAPO_TEST_DOCKER_FAIL_MATCH
SSH_ORIGINAL_COMMAND="activate $failed_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Pull-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Pull-Fehler änderte current'
PAPO_TEST_DOCKER_FAIL_MATCH='up -d --no-deps backend'; export PAPO_TEST_DOCKER_FAIL_MATCH
SSH_ORIGINAL_COMMAND="activate $failed_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Compose-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Compose-Fehler änderte current'
PAPO_TEST_DOCKER_FAIL_MATCH='__never__'; export PAPO_TEST_DOCKER_FAIL_MATCH
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$sha" ] || bad 'Compose-Fehler ohne Rollback'

PAPO_TEST_CURL_READY_AFTER=1; export PAPO_TEST_CURL_READY_AFTER
: > "$PAPO_TEST_CURL_COUNT_FILE"
run_ok 'verzögerte Bereitschaft' "activate $failed_sha"
unset PAPO_TEST_CURL_READY_AFTER
[ "$(readlink "$DEPLOY/frontend-live/current")" = "releases/$failed_sha" ] || bad 'verzögerte Bereitschaft ohne Frontend-Wechsel'

old_target=$(readlink "$DEPLOY/frontend-live/current"); publish unhealthy; unhealthy_sha=$(git -C "$SOURCE" rev-parse HEAD); archive_for "$unhealthy_sha" "$archive"; run_ok 'Upload ungesund' "upload $unhealthy_sha" "$archive"
PAPO_TEST_CURL_FAIL_IMAGE_TAG=$unhealthy_sha; export PAPO_TEST_CURL_FAIL_IMAGE_TAG
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'ungesundes Backend akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'ungesundes Backend änderte current'
unset PAPO_TEST_CURL_FAIL_IMAGE_TAG
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" ] || bad 'ungesundes Backend ohne Rollback'

PAPO_TEST_LINK_FAIL=1; export PAPO_TEST_LINK_FAIL
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Frontend-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Frontend-Fehler änderte current'
unset PAPO_TEST_LINK_FAIL
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" ] || bad 'Frontend-Fehler ohne Rollback'

PAPO_TEST_CURL_FAIL_IMAGE_TAG=$unhealthy_sha PAPO_TEST_FAIL_UP_TAG=$failed_sha; export PAPO_TEST_CURL_FAIL_IMAGE_TAG PAPO_TEST_FAIL_UP_TAG
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Rollback-Compose-Fehler akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Rollback-Compose-Fehler änderte current'
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$unhealthy_sha" ] || bad 'Rollback-Fehler nicht erkennbar'
unset PAPO_TEST_FAIL_UP_TAG

printf '%s\n' 'ghcr.io/chris-vahram/diplomarbeit-papo-backend:latest' > "$PAPO_TEST_IMAGE_FILE"
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'unsicheres Rollback-Image akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'fehlendes Rollback-Image änderte current'
printf '%s\n' "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" > "$PAPO_TEST_IMAGE_FILE"
unset PAPO_TEST_CURL_FAIL_IMAGE_TAG

PAPO_TEST_INSPECT_IMAGE_OVERRIDE='ghcr.io/chris-vahram/diplomarbeit-papo-backend:latest'; export PAPO_TEST_INSPECT_IMAGE_OVERRIDE
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'falsches Inspect-Image akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'falsches Inspect-Image änderte current'
unset PAPO_TEST_INSPECT_IMAGE_OVERRIDE

PAPO_TEST_INSPECT_CHURN=1; export PAPO_TEST_INSPECT_CHURN
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'wechselnder Container akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'wechselnder Container änderte current'
unset PAPO_TEST_INSPECT_CHURN

printf '%s\n' "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" > "$PAPO_TEST_IMAGE_FILE"
PAPO_TEST_SIGNAL_ON_UP=1; export PAPO_TEST_SIGNAL_ON_UP
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'abgebrochene Aktivierung akzeptiert' || ok
[ "$(readlink "$DEPLOY/frontend-live/current")" = "$old_target" ] || bad 'Signal änderte current'
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" ] || bad 'Signal löste Backend-Rollback nicht aus'
unset PAPO_TEST_SIGNAL_ON_UP

PAPO_TEST_CURL_FAIL_IMAGE_TAG=$unhealthy_sha PAPO_TEST_SIGNAL_ON_RESTORE=1 PAPO_TEST_SIGNAL_TWICE_ON_RESTORE=1 PAPO_TEST_RESTORE_TAG=$failed_sha
export PAPO_TEST_CURL_FAIL_IMAGE_TAG PAPO_TEST_SIGNAL_ON_RESTORE PAPO_TEST_SIGNAL_TWICE_ON_RESTORE PAPO_TEST_RESTORE_TAG
curl_count_before=$(sed -n 1p "$PAPO_TEST_CURL_COUNT_FILE")
SSH_ORIGINAL_COMMAND="activate $unhealthy_sha" "$GATEWAY" >/dev/null 2>&1 && bad 'Signal beim Rollback akzeptiert' || ok
[ -f "$PAPO_TEST_RESTORE_SIGNAL_FILE" ] || bad 'Signal beim Rollback nicht ausgelöst'
[ -f "$PAPO_TEST_RESTORE_SECOND_SIGNAL_FILE" ] || bad 'zweites Signal beim Rollback nicht ausgelöst'
[ "$(sed -n 1p "$PAPO_TEST_IMAGE_FILE")" = "ghcr.io/chris-vahram/diplomarbeit-papo-backend:$failed_sha" ] || bad 'Signal unterbrach Backend-Rollback'
curl_count_after=$(sed -n 1p "$PAPO_TEST_CURL_COUNT_FILE")
[ "$curl_count_after" -ge "$((curl_count_before + 5))" ] || bad 'zweites Signal unterbrach Bereitschaftsprüfung des Rollbacks'
ok
unset PAPO_TEST_CURL_FAIL_IMAGE_TAG PAPO_TEST_SIGNAL_ON_RESTORE PAPO_TEST_SIGNAL_TWICE_ON_RESTORE PAPO_TEST_RESTORE_TAG

chmod 644 "$DEPLOY/.env.prod"
run_fail 'unsichere env-Datei' "activate $failed_sha"

if [ "$failures" -ne 0 ]; then printf '%s\n' "$failures Harness-Prüfung(en) fehlgeschlagen" >&2; exit 1; fi
printf '%s\n' "$pass Harness-Prüfungen bestanden"
