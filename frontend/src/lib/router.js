import { isAuthenticated, getSession, ensureValidSession, logout } from "./auth.js";
import { apiFetch } from "./api.js";
import { escapeHtml } from "./html.js";

// Route table: hash path -> { tag, role, title }
// role: null = public, "company" | "partner" = required realm role for FR-20 deep-link protection.
const routes = {
  "/login": { tag: "login-page", role: null, title: "Anmeldung" },
  "/company/dashboard": { tag: "company-dashboard-page", role: "company", title: "Firmen-Dashboard" },
  "/company/products": { tag: "company-product-list-page", role: "company", title: "Produkte" },
  "/company/partners": { tag: "company-partner-list-page", role: "company", title: "Partnerverwaltung" },
  "/company/orders": { tag: "company-order-list-page", role: "company", title: "Bestellungen" },
  "/company/import": { tag: "company-import-page", role: "company", title: "Datenimport" },
  "/partner/dashboard": { tag: "partner-dashboard-page", role: "partner", title: "Partner-Dashboard" },
  "/partner/products": { tag: "partner-product-list-page", role: "partner", title: "Produkte" },
  "/partner/orders": { tag: "partner-order-list-page", role: "partner", title: "Meine Bestellungen" },
  "/partner/orders/new": { tag: "partner-order-new-page", role: "partner", title: "Neue Bestellung" },
  "/partner/profile": { tag: "partner-profile-page", role: "partner", title: "Mein Profil" },
};

const NAV_ICONS = {
  "/company/dashboard": "layout-dashboard",
  "/company/products": "package",
  "/company/partners": "users",
  "/company/orders": "shopping-cart",
  "/company/import": "upload",
  "/partner/dashboard": "layout-dashboard",
  "/partner/products": "package",
  "/partner/orders": "shopping-cart",
  "/partner/profile": "circle-user",
};

const appShell = document.getElementById("app-shell");
const appRoot = document.getElementById("app-root");
const appNav = document.getElementById("app-nav");
const appUser = document.getElementById("app-user");
const appRole = document.getElementById("app-role");
const appBackdrop = document.getElementById("app-backdrop");
const navToggle = document.getElementById("nav-toggle");

// Off-canvas sidebar (narrow viewports only — see base.css breakpoint). Closing
// on every navigation/backdrop-click/Escape means the mobile state never
// leaks into a desktop resize or the next route.
function closeMobileNav() {
  appShell.classList.remove("nav-open");
  navToggle?.setAttribute("aria-expanded", "false");
}

navToggle?.addEventListener("click", () => {
  const isOpen = appShell.classList.toggle("nav-open");
  navToggle.setAttribute("aria-expanded", String(isOpen));
});
appBackdrop?.addEventListener("click", closeMobileNav);
appNav.addEventListener("click", (event) => {
  if (event.target.closest("a")) closeMobileNav();
});
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") closeMobileNav();
});

function currentPath() {
  return location.hash.replace(/^#/, "") || "/login";
}

function resolveDefaultRoute() {
  const session = getSession();
  if (!session) return "/login";
  if (session.isCompany) return "/company/dashboard";
  return "/partner/dashboard";
}

function initials(username) {
  const parts = String(username ?? "").split(/[.\s_-]+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}

function closeProfileMenu() {
  const panel = appUser?.querySelector(".profile-menu__panel");
  const button = appUser?.querySelector(".profile-menu__button");
  if (panel) panel.hidden = true;
  if (button) button.setAttribute("aria-expanded", "false");
}

document.addEventListener("click", () => closeProfileMenu());

function renderNav() {
  const session = getSession();
  appShell.classList.remove("context-company", "context-partner");
  appNav.innerHTML = "";
  appUser.innerHTML = "";
  appRole.textContent = "";

  if (!session) return;

  appShell.classList.add(session.isCompany ? "context-company" : "context-partner");
  appRole.textContent = "Partnerportal";

  const links = session.isCompany
    ? [
        ["/company/dashboard", "Dashboard"],
        ["/company/orders", "Bestellungen"],
        ["/company/partners", "Partnerverwaltung"],
        ["/company/products", "Produkte"],
        ["/company/import", "Datenimport"],
      ]
    : [
        ["/partner/dashboard", "Dashboard"],
        ["/partner/orders", "Meine Bestellungen"],
        ["/partner/products", "Produkte"],
      ];

  const path = currentPath();
  for (const [href, label] of links) {
    const link = document.createElement("a");
    link.href = `#${href}`;
    link.className = href === path ? "is-active" : "";
    link.innerHTML = `<i data-lucide="${NAV_ICONS[href]}"></i><span>${label}</span>`;
    appNav.appendChild(link);
  }

  const menu = document.createElement("div");
  menu.className = "profile-menu";
  menu.innerHTML = `
    <button type="button" class="profile-menu__button" aria-haspopup="true" aria-expanded="false">
      <span class="profile-menu__avatar">${escapeHtml(initials(session.username))}</span>
      <i data-lucide="chevron-down" class="profile-menu__caret"></i>
    </button>
    <div class="profile-menu__panel" hidden>
      <div class="profile-menu__identity">
        <div class="profile-menu__name">${escapeHtml(session.username)}</div>
        <div class="profile-menu__role">${session.isCompany ? "Firma" : "Partner"}</div>
      </div>
      <button type="button" data-action="profile" class="profile-menu__item">
        <i data-lucide="settings"></i><span>Einstellungen</span>
      </button>
      <button type="button" data-action="logout" class="profile-menu__item profile-menu__item--danger">
        <i data-lucide="log-out"></i><span>Abmelden</span>
      </button>
    </div>
  `;
  appUser.appendChild(menu);

  const button = menu.querySelector(".profile-menu__button");
  const panel = menu.querySelector(".profile-menu__panel");
  button.addEventListener("click", (event) => {
    event.stopPropagation();
    const isOpen = !panel.hidden;
    panel.hidden = isOpen;
    button.setAttribute("aria-expanded", String(!isOpen));
  });
  menu.querySelector('[data-action="logout"]').addEventListener("click", () => {
    logout();
    location.hash = "#/login";
  });
  menu.querySelector('[data-action="profile"]').addEventListener("click", () => {
    closeProfileMenu();
    location.hash = session.isCompany ? "#/company/dashboard" : "#/partner/profile";
  });

  window.lucide?.createIcons();
  refreshOrdersBadge();
}

// Unread-order count for the company sidebar "Bestellungen" nav item
// (order-notifications spec). Fetched from a dedicated count endpoint (not the full
// order list, which this runs on every nav re-render) — no polling/websocket, per design.md.
export async function refreshOrdersBadge() {
  const session = getSession();
  const link = appNav.querySelector('a[href="#/company/orders"]');
  if (!session?.isCompany || !link) return;
  try {
    const unseenCount = await apiFetch("/company/orders/unseen-count");
    let badge = link.querySelector(".nav-badge");
    if (unseenCount > 0) {
      if (!badge) {
        badge = document.createElement("span");
        badge.className = "nav-badge";
        link.appendChild(badge);
      }
      badge.textContent = unseenCount > 99 ? "99+" : String(unseenCount);
    } else {
      badge?.remove();
    }
  } catch {
    // Badge is a non-critical enhancement — a failed fetch just leaves it as-is.
  }
}

// render() awaits ensureValidSession(), so a fast second navigation can start before
// the first one's continuation resumes - this token lets a superseded render bail out
// instead of overwriting the DOM with a stale route.
let renderGeneration = 0;

async function render() {
  const myGeneration = ++renderGeneration;
  closeMobileNav();
  const path = currentPath();
  const route = routes[path];

  if (!route) {
    location.hash = `#${resolveDefaultRoute()}`;
    return;
  }

  // FR-20: protected views must not be reachable via direct URL without authorization.
  // ensureValidSession() refreshes an expired-but-refreshable access token first, so a
  // partner mid-navigation isn't bounced to login just because the old token lapsed.
  if (route.role) {
    const session = await ensureValidSession();
    if (myGeneration !== renderGeneration) return;
    if (!session) {
      location.hash = "#/login";
      return;
    }
    if (!session.roles.includes(route.role)) {
      location.hash = `#${resolveDefaultRoute()}`;
      return;
    }
  }
  if (path === "/login" && isAuthenticated()) {
    location.hash = `#${resolveDefaultRoute()}`;
    return;
  }

  document.title = `Partnerportal – ${route.title}`;
  appShell.classList.toggle("app-shell--bare", path === "/login");
  appRoot.innerHTML = "";
  appRoot.appendChild(document.createElement(route.tag));
  renderNav();
}

window.addEventListener("hashchange", render);
window.addEventListener("DOMContentLoaded", () => {
  import("../components/index.js");
  import("../pages/login-page.js");
  import("../pages/company-dashboard-page.js");
  import("../pages/company-product-list-page.js");
  import("../pages/company-partner-list-page.js");
  import("../pages/company-order-list-page.js");
  import("../pages/company-import-page.js");
  import("../pages/partner-dashboard-page.js");
  import("../pages/partner-product-list-page.js");
  import("../pages/partner-order-list-page.js");
  import("../pages/partner-order-new-page.js");
  import("../pages/partner-profile-page.js");
  render();
});
