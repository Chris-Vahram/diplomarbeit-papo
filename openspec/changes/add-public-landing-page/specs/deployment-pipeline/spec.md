## MODIFIED Requirements

### Requirement: Production topology — single VM, containerized services
The system SHALL run the frontend, backend, Postgres, Keycloak, and reverse proxy as containers via Docker Compose on a single self-managed VPS, with the frontend and API served from the same origin.

#### Scenario: Single-origin routing
- **WHEN** a client requests the production domain
- **THEN** Caddy serves the public landing page at exactly `/`
- **AND** serves the portal entry (`index.html`) at `/portal/` and redirects `/portal` to `/portal/`
- **AND** serves the static pages `/impressum`, `/datenschutz` and `/robots.txt`
- **AND** keeps serving `shop.html` for `/shop`, `/shop/*` and `/shop.html`
- **AND** routes `/api/*` to the Quarkus container
- **AND** routes `/auth/*` to the Keycloak container
- **AND** serves existing static files (scripts, styles, images, fonts) unchanged
- **AND** answers every other path with the static not-found page and HTTP status 404 instead of the portal entry
- **AND** no cross-origin (CORS) configuration is required for frontend-to-API calls

#### Scenario: Local routing matches production
- **WHEN** a developer runs `tools/dev_frontend_server.py`
- **THEN** `/`, `/portal/`, `/impressum`, `/datenschutz`, shop paths and unknown paths are answered the same way as by Caddy, including the 404 status

#### Scenario: Automatic HTTPS
- **WHEN** the production domain is requested over HTTP or HTTPS
- **THEN** Caddy automatically provisions and renews a Let's Encrypt certificate and serves the site over HTTPS

#### Scenario: Keycloak realm reproducibility
- **WHEN** the Keycloak container starts
- **THEN** it imports the versioned realm JSON export (`backend/src/main/resources/keycloak/partnerportal-realm.json`)
- **AND** the imported realm's `redirectUris`/`webOrigins` are scoped to the production domain, not a wildcard

#### Scenario: Shared Postgres instance, isolated databases
- **WHEN** the Postgres container starts for the first time
- **THEN** it provisions two databases, `partnerportal` and `keycloak`, within the same Postgres instance
- **AND** the application and Keycloak each connect only to their own database
