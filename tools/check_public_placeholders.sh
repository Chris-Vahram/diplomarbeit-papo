#!/bin/sh
# Lists every remaining [[…]] placeholder in the public pages (add-public-landing-page).
# Exit code 1 while any is left, so no placeholder goes live unnoticed.
cd "$(dirname "$0")/../frontend" || exit 2
if grep -n -o '\[\[[A-Z_]*\]\]' landing.html impressum.html datenschutz.html 404.html; then
  echo "Offene Platzhalter gefunden (siehe oben)." >&2
  exit 1
fi
echo "Keine Platzhalter in den öffentlichen Seiten."
