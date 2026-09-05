# Cold-Restart-Handoffs

Hier liegt bei einem Modellwechsel oder hohem Kontextverbrauch genau ein kurzer Handoff pro Change und Phase, benannt als `<change-id>-<phase>.md`.

```markdown
# <change-id> — <phase>

- Datum, Modell/Reasoning, aktiver Branch
- Ziel und ausdrücklich ausgeschlossener Scope
- gelesene maßgebliche Quellen
- getroffene Entscheidungen und offene Fragen
- OpenSpec-Status und nächste offene Task-ID
- geänderte Dateien
- wirklich ausgeführte Checks mit Ergebnis
- externe/interaktive Blocker ohne Secretwerte oder geheime Links
- nächster exakter Prompt/Schritt
```

Regeln: maximal etwa eine Bildschirmseite, Quellen verlinken statt kopieren, keine Vermutungen als Fakten, keine Credentials. Nach Abschluss löschen oder als knappe Projekthistorie belassen; niemals als Ersatz für `tasks.md`, Specs oder Tests verwenden.
