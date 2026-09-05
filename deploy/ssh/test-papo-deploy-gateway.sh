#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GATEWAY="$SCRIPT_DIR/papo-deploy-gateway"
TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/papo-gateway-test.XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT HUP INT TERM

SOURCE="$TEST_ROOT/source"
ORIGIN="$TEST_ROOT/origin.git"
STATE="$TEST_ROOT/state"
DEPLOY="$TEST_ROOT/deploy"
STUBS="$TEST_ROOT/stubs"
LOG="$TEST_ROOT/docker.log"
mkdir -p "$SOURCE" "$STATE/incoming" "$STATE/ssh" "$DEPLOY/releases" "$STUBS"
: > "$LOG"

cat > "$STUBS/flock" <<'EOF'
#!/bin/sh
[ "${PAPO_TEST_LOCK_FAIL-}" != 1 ]
EOF
cat > "$STUBS/runuser" <<'EOF'
#!/bin/sh
[ "$1" = --user ] && shift 2
[ "$1" = -- ] && shift
exec "$@"
EOF
cat > "$STUBS/docker" <<'EOF'
#!/bin/sh
printf '%s\n' "$*" >> "$PAPO_TEST_LOG"
case "$*" in *"${PAPO_TEST_DOCKER_FAIL_MATCH:?unused}"*) exit 1 ;; esac
EOF
cat > "$STUBS/curl" <<'EOF'
#!/bin/sh
case "${PAPO_TEST_HEALTH-}" in
bad) printf 503 ;;
*) case "$*" in */api/partner/dashboard*) printf 401 ;; *) printf 200 ;; esac ;;
esac
EOF
cat > "$STUBS/sleep" <<'EOF'
#!/bin/sh
:
EOF
chmod 700 "$STUBS"/*

export PATH="$STUBS:$PATH"
export PAPO_DEPLOY_TESTING=1 PAPO_TEST_ROOT="$TEST_ROOT" PAPO_TEST_ORIGIN="$ORIGIN"
export PAPO_TEST_LOG="$LOG" PAPO_TEST_DOCKER_FAIL_MATCH='__never__'

git -C "$SOURCE" init -q
git -C "$SOURCE" config user.name 'Gateway Test'
git -C "$SOURCE" config user.email 'gateway@example.invalid'
mkdir -p "$SOURCE/deploy" "$SOURCE/frontend"
printf '%s\n' 'services: {}' > "$SOURCE/docker-compose.prod.yml"
printf '%s\n' '#!/bin/sh' 'exit 0' > "$SOURCE/deploy/example.sh"
printf '%s\n' '<!doctype html>' > "$SOURCE/frontend/index.html"
git -C "$SOURCE" add docker-compose.prod.yml deploy frontend
git -C "$SOURCE" commit -qm initial
git init -q --bare "$ORIGIN"
git -C "$SOURCE" remote add origin "$ORIGIN"
git -C "$SOURCE" branch -M main
git -C "$SOURCE" push -q -u origin main
git clone -q --bare "$ORIGIN" "$STATE/repository.git"
printf '%s\n' 'DOMAIN=example.invalid' > "$DEPLOY/.env.prod"
chmod 600 "$DEPLOY/.env.prod"

pass=0
failures=0
ok() { pass=$((pass + 1)); }
not_ok() { printf 'FEHLER: %s\n' "$1" >&2; failures=$((failures + 1)); }

run_ok() {
    label=$1 command=$2 input=${3-}
    if [ -n "$input" ]; then
        SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" < "$input" >/dev/null 2>&1 && ok || not_ok "$label"
    else
        SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1 && ok || not_ok "$label"
    fi
}

run_fail() {
    label=$1 command=$2 input=${3-}
    before=$(wc -l < "$LOG" | tr -d ' ')
    if [ -n "$input" ]; then
        SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" < "$input" >/dev/null 2>&1 && not_ok "$label" || ok
    else
        SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1 && not_ok "$label" || ok
    fi
    after=$(wc -l < "$LOG" | tr -d ' ')
    [ "$before" = "$after" ] || not_ok "$label führte Docker vor Validierungsende aus"
}

run_runtime_fail() {
    label=$1 command=$2
    SSH_ORIGINAL_COMMAND="$command" "$GATEWAY" >/dev/null 2>&1 && not_ok "$label" || ok
}

archive_for() {
    git -C "$SOURCE" archive --format=tar --output="$2" "$1" -- docker-compose.prod.yml deploy frontend
}

publish_commit() {
    printf '%s\n' "$1" >> "$SOURCE/frontend/index.html"
    git -C "$SOURCE" add frontend/index.html
    git -C "$SOURCE" commit -qm "$1"
    git -C "$SOURCE" push -q origin main
}

sha=$(git -C "$SOURCE" rev-parse HEAD)
archive="$TEST_ROOT/release.tar"
archive_for "$sha" "$archive"

run_fail 'fehlender Befehl' ''
run_fail 'unbekannte Operation' "shell $sha"
run_fail 'zusätzliches Argument' "verify $sha extra"
run_fail 'zusätzliches Leerzeichen' "verify  $sha"
upper=$(printf '%s' "$sha" | tr 'a-f' 'A-F')
run_fail 'nicht-kleine SHA' "verify $upper"

printf x > "$TEST_ROOT/not-canonical.tar"
run_fail 'nicht-kanonisches Archiv' "upload $sha" "$TEST_ROOT/not-canonical.tar"
dd if=/dev/zero of="$TEST_ROOT/oversize.tar" bs=1048576 count=25 2>/dev/null
printf x >> "$TEST_ROOT/oversize.tar"
run_fail 'Archiv über 25 MiB' "upload $sha" "$TEST_ROOT/oversize.tar"

PAPO_TEST_LOCK_FAIL=1; export PAPO_TEST_LOCK_FAIL
run_fail 'Lock-Konflikt' "upload $sha" "$archive"
unset PAPO_TEST_LOCK_FAIL

run_ok 'gültiger Upload' "upload $sha" "$archive"
releases_before=$(find "$DEPLOY/releases" -mindepth 1 -maxdepth 1 | wc -l | tr -d ' ')
docker_before=$(wc -l < "$LOG" | tr -d ' ')
run_ok 'gültiges Verify' "verify $sha"
[ "$releases_before" = "$(find "$DEPLOY/releases" -mindepth 1 -maxdepth 1 | wc -l | tr -d ' ')" ] || not_ok 'Verify erzeugte Release'
[ "$docker_before" = "$(wc -l < "$LOG" | tr -d ' ')" ] || not_ok 'Verify führte Docker aus'

mv "$ORIGIN" "$TEST_ROOT/origin.off"
run_fail 'Fetch-Fehler' "verify $sha"
mv "$TEST_ROOT/origin.off" "$ORIGIN"

publish_commit second
current_sha=$(git -C "$SOURCE" rev-parse HEAD)
current_archive="$TEST_ROOT/current.tar"
archive_for "$current_sha" "$current_archive"
run_fail 'nicht aktueller main-SHA' "upload $sha" "$archive"

ln -s index.html "$SOURCE/frontend/link"
git -C "$SOURCE" add frontend/link
git -C "$SOURCE" commit -qm nonblob
git -C "$SOURCE" push -q origin main
nonblob_sha=$(git -C "$SOURCE" rev-parse HEAD)
archive_for "$nonblob_sha" "$TEST_ROOT/nonblob.tar"
run_fail 'nicht-regulärer Tree-Eintrag' "upload $nonblob_sha" "$TEST_ROOT/nonblob.tar"
git -C "$SOURCE" rm -q frontend/link
git -C "$SOURCE" commit -qm regular-again
git -C "$SOURCE" push -q origin main
sha=$(git -C "$SOURCE" rev-parse HEAD)
archive_for "$sha" "$archive"
run_ok 'Upload vor Aktivierung' "upload $sha" "$archive"

mv "$DEPLOY/.env.prod" "$TEST_ROOT/env.saved"
run_fail 'fehlende Produktionsumgebung' "activate $sha"
mv "$TEST_ROOT/env.saved" "$DEPLOY/.env.prod"
chmod 644 "$DEPLOY/.env.prod"
run_fail 'falscher Umgebungsmodus' "activate $sha"
chmod 600 "$DEPLOY/.env.prod"
printf '%s\n' 'DOMAIN=bad value' > "$DEPLOY/.env.prod"
run_fail 'ungültige Domain' "activate $sha"
printf '%s\n' 'DOMAIN=example.invalid' > "$DEPLOY/.env.prod"
chmod 600 "$DEPLOY/.env.prod"
printf x > "$DEPLOY/current"
run_fail 'current ist kein Symlink' "activate $sha"
rm "$DEPLOY/current"

: > "$LOG"
run_ok 'erster Deploy' "activate $sha"
[ "$(sed -n '1p' "$LOG")" = 'compose -f docker-compose.prod.yml --env-file '"$DEPLOY"'/.env.prod pull' ] || not_ok 'First-deploy pull falsch'
[ "$(sed -n '2p' "$LOG")" = 'compose -f docker-compose.prod.yml --env-file '"$DEPLOY"'/.env.prod up -d' ] || not_ok 'First-deploy up falsch'
[ "$(readlink "$DEPLOY/current")" = "$DEPLOY/releases/$sha" ] || not_ok 'First-deploy current falsch'

publish_commit routine
routine_sha=$(git -C "$SOURCE" rev-parse HEAD)
archive_for "$routine_sha" "$archive"
run_ok 'Routine-Upload' "upload $routine_sha" "$archive"
: > "$LOG"
run_ok 'Routine-Deploy' "activate $routine_sha"
[ "$(sed -n '1p' "$LOG")" = 'compose -f docker-compose.prod.yml --env-file '"$DEPLOY"'/.env.prod pull backend' ] || not_ok 'Routine pull falsch'
[ "$(sed -n '2p' "$LOG")" = 'compose -f docker-compose.prod.yml --env-file '"$DEPLOY"'/.env.prod up -d --no-deps backend caddy' ] || not_ok 'Routine up falsch'
[ "$(readlink "$DEPLOY/current")" = "$DEPLOY/releases/$routine_sha" ] || not_ok 'Routine current falsch'
[ -e "$DEPLOY/.current.$routine_sha.$$" ] && not_ok 'atomarer current-Temp-Link blieb bestehen' || ok

old_target=$(readlink "$DEPLOY/current")
publish_commit pull-failure
failed_sha=$(git -C "$SOURCE" rev-parse HEAD)
archive_for "$failed_sha" "$archive"
run_ok 'Upload für Pull-Fehler' "upload $failed_sha" "$archive"
PAPO_TEST_DOCKER_FAIL_MATCH='pull backend'; export PAPO_TEST_DOCKER_FAIL_MATCH
run_runtime_fail 'fehlgeschlagener Pull' "activate $failed_sha"
[ "$(readlink "$DEPLOY/current")" = "$old_target" ] || not_ok 'Pull-Fehler änderte current'

PAPO_TEST_DOCKER_FAIL_MATCH='up -d --no-deps'; export PAPO_TEST_DOCKER_FAIL_MATCH
run_runtime_fail 'fehlgeschlagenes Up' "activate $failed_sha"
[ "$(readlink "$DEPLOY/current")" = "$old_target" ] || not_ok 'Up-Fehler änderte current'

PAPO_TEST_DOCKER_FAIL_MATCH='__never__'; PAPO_TEST_HEALTH=bad
export PAPO_TEST_DOCKER_FAIL_MATCH PAPO_TEST_HEALTH
run_runtime_fail 'fehlgeschlagene Healthchecks' "activate $failed_sha"
[ "$(readlink "$DEPLOY/current")" = "$old_target" ] || not_ok 'Health-Fehler änderte current'
unset PAPO_TEST_HEALTH

if [ "$failures" -ne 0 ]; then
    printf '%s\n' "$failures Harness-Prüfung(en) fehlgeschlagen" >&2
    exit 1
fi
printf '%s\n' "$pass Harness-Prüfungen bestanden"
