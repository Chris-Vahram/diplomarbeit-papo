# Steueridentifikatoren

Stand: 14.09.2026. PAPO speichert je Firmenprofil und Partner genau eine
Steueridentifikation: ISO-3166-1-Alpha-2-Land, explizite Steuerart und Wert.
Der Wert wird getrimmt und großgeschrieben. Die Prüfung ist ausschließlich eine
Syntaxprüfung; PAPO bestätigt keine Eintragung in einem Register und fragt weder
VIES noch BMD ab.

## Österreichische Regeln

- `AT` + `VAT_ID`: `ATU` plus acht Ziffern, z. B. `ATU12345678`.
- `AT` + `NATIONAL_TAX_ID`: genau neun Ziffern, z. B. `123456789`.
- Andere Länder und beide unterstützten Arten: 2–64 Zeichen aus `A–Z`, Ziffern,
  Leerzeichen, `.`, `_`, `/` und `-`. Das ist eine konservative Eingabeform,
  keine länderspezifische Gültigkeitsprüfung.

Die österreichische UID-Syntax (`ATU12345678`) ist in der BMF-Übersicht zum
Aufbau der UID dokumentiert:
<https://www.bmf.gv.at/dam/jcr%3A77d9806a-8340-424e-a322-cdbe144b086c/BMF-BR-ST-UID_2017.pdf>
(abgerufen am 14.09.2026). Die neunstellige österreichische Steuernummer wird
vom BMF für Finanzamtszahlungen und in der Informationsbroschüre des Finanzamts
Österreich ausgewiesen:
<https://www.bmf.gv.at/themen/steuern/fristen-verfahren/steuerzahlungen.html>
und
<https://www.bmf.gv.at/dam/jcr%3Aad0e0b62-9d3b-4b6e-87a5-1d69ba88afa3/BMF-BR-Folder_Modernisierung_20210107.pdf>
(abgerufen am 14.09.2026).

## Testvektoren

| Eingabe | Erwartung |
|---|---|
| `at`, `VAT_ID`, ` atu12345678 ` | akzeptiert als `AT`, `ATU12345678` |
| `AT`, `NATIONAL_TAX_ID`, `123456789` | akzeptiert |
| `AT`, `VAT_ID`, `AT12345678` | abgelehnt |
| `AT`, `NATIONAL_TAX_ID`, `12345678` | abgelehnt |
| `DE`, `VAT_ID`, `de123456789` | akzeptiert als `DE123456789` (Syntax only) |
| `DE`, `VAT_ID`, `DE/12?` | abgelehnt |
| jede Kombination mit fehlendem Land, Typ oder Wert | abgelehnt |

Demo-Werte sind absichtlich synthetisch und stellen keine Registereinträge dar.
