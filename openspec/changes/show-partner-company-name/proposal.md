## Why

Das Originalprotokoll vom 10.09.2026 verlangt die sichtbare Partnerfirma statt allgemeiner Portalüberschriften. Chris bestätigt in dieser Sitzung das bestehende Feld `Partner.name` als Firmenname; hierfür ist kein Company-/Tenant-Modell nötig.

## What Changes

- Partnerdashboard: „Dashboard der Firma <Name>“.
- Linke Portalbeschriftung: „Partnerportal der Firma <Name>“ aus dem eigenen Profil.
- Sichere neutrale Anzeige bei Laden, Fehler und Sessionwechsel; kein persistenter Namenscache.

## Capabilities

### New Capabilities

Keine.

### Modified Capabilities

- `frontend-shell`: partnerbezogene Portalbeschriftung.
- `partner-dashboard`: Firmenname in der Dashboardüberschrift.

## Impact

`frontend/src/lib/router.js`, `frontend/src/pages/partner-dashboard-page.js`, nötigenfalls eng begrenzte CSS-Regeln. Bestehendes `GET /api/partner/profile`, keine API-/Daten-/Authänderung. Selbst editierbarer Anzeigename, keine verifizierte Identität.

## Non-goals

Company-Personalisierung, Logo, Browser-Tab-Personalisierung, Ansprechpartner, Loginmigration, Mandantenfähigkeit und Partnershop.
