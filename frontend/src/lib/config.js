// Single source of truth for environment-dependent base URLs. Production serves
// frontend, backend and Keycloak from one origin via path-based routing (Caddy:
// "/" -> frontend, "/api/*" -> backend, "/auth/*" -> Keycloak), so relative paths
// derived from the current origin work there unchanged for any domain. Local dev
// runs the frontend on its own static server (docs/SETUP.md: :3000), separate
// from the backend (:8080) and Keycloak (:8081), so those need absolute URLs.
const isLocalDev = location.hostname === "localhost" && location.port === "3000";

export const API_BASE = isLocalDev ? "http://localhost:8080/api" : `${location.origin}/api`;
export const KEYCLOAK_BASE = isLocalDev ? "http://localhost:8081" : `${location.origin}/auth`;
