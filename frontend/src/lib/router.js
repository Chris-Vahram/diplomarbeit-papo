import { isAuthenticated, getSession, ensureValidSession, logout } from "./auth.js";
import { apiFetch } from "./api.js";
import { escapeHtml } from "./html.js";

// Route table: hash path -> { tag, role, title, fullscreen? }
// role: null = public, "company" | "partner" = required realm role for FR-20 deep-link protection.
// fullscreen: no portal sidebar/topbar; the page brings its own way back (shop builder).
const routes = {
  "/login": { tag: "login-page", role: null, title: "Anmeldung" },
  "/company/dashboard": { tag: "company-dashboard-page", role: "company", title: "Firmen-Dashboard" },
  "/company/profile": { tag: "company-profile-page", role: "company", title: "Firmenprofil" },
  "/company/products": { tag: "company-product-list-page", role: "company", title: "Artikelverwaltung" },
  "/company/categories": { tag: "company-category-list-page", role: "company", title: "Artikelkategorien" },
  "/company/partners": { tag: "company-partner-list-page", role: "company", title: "Partnerverwaltung" },
  "/company/orders": { tag: "company-order-list-page", role: "company", title: "Bestellungen" },
  "/company/import": { tag: "company-import-page", role: "company", title: "Datenimport" },
  "/partner/dashboard": { tag: "partner-dashboard-page", role: "partner", title: "Partner-Dashboard" },
  "/partner/products": { tag: "partner-product-list-page", role: "partner", title: "Meine Artikel" },
  "/partner/orders": { tag: "partner-order-list-page", role: "partner", title: "Meine Bestellungen" },
  "/partner/orders/new": { tag: "partner-order-new-page", role: "partner", title: "Neue Bestellung" },
  "/partner/customers": { tag: "partner-customer-list-page", role: "partner", title: "Meine Endkunden" },
  "/partner/shop": { tag: "partner-shop-page", role: "partner", title: "Mein Webshop" },
  "/partner/shop/inquiries": { tag: "partner-shop-inquiries-page", role: "partner", title: "Anfragen" },
  "/partner/shop/editor": { tag: "partner-shop-editor-page", role: "partner", title: "Shop bearbeiten", fullscreen: true },
  "/partner/profile": { tag: "partner-profile-page", role: "partner", title: "Mein Profil" },
};

const NAV_ICONS = {
  "/company/dashboard": "layout-dashboard",
  "/company/profile": "building-2",
  "/company/products": "package",
  "/company/categories": "tags",
  "/company/partners": "users",
  "/company/orders": "shopping-cart",
  "/company/import": "upload",
  "/partner/dashboard": "layout-dashboard",
  "/partner/products": "package",
  "/partner/orders": "shopping-cart",
  "/partner/customers": "contact",
  "/partner/shop": "store",
  "/partner/shop/inquiries": "inbox",
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
  return location.hash.replace(/^#/, "").split("?")[0] || "/login";
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

// Order-document print (add-order-document-print): the button lives inside the
// shared confirmation/details HTML fragments (order-form.js, order-details.js),
// always immediately followed by its .print-document sibling - one delegated
// listener here covers every mount point without per-page wiring. The document
// is often printed from inside a modal-dialog; cloning it into a fresh
// top-level #print-root (removed again afterwards) sidesteps the modal's
// positioning/overflow so a long document paginates correctly (base.css).
let printSource = null;

document.addEventListener("click", (event) => {
  const printButton = event.target.closest("[data-action='print']");
  if (!printButton) return;
  printSource = printButton.previousElementSibling;
  window.print();
});

window.addEventListener("beforeprint", () => {
  if (!printSource) return;
  document.getElementById("print-root")?.remove();
  const printRoot = document.createElement("div");
  printRoot.id = "print-root";
  printRoot.innerHTML = printSource.innerHTML;
  document.body.appendChild(printRoot);
});

window.addEventListener("afterprint", () => {
  document.getElementById("print-root")?.remove();
  printSource = null;
});

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
        ["/company/profile", "Firmenprofil"],
        ["/company/orders", "Bestellungen"],
        ["/company/partners", "Partnerverwaltung"],
        ["/company/products", "Artikelverwaltung"],
        ["/company/import", "Datenimport"],
      ]
    : [
        ["/partner/dashboard", "Dashboard"],
        ["/partner/orders", "Meine Bestellungen"],
        ["/partner/customers", "Meine Endkunden"],
        ["/partner/products", "Meine Artikel"],
        ["/partner/shop", "Mein Webshop"],
        // Sub-item of "Mein Webshop" (shop-inquiries spec), indented in the sidebar.
        ["/partner/shop/inquiries", "Anfragen", "nav-sub"],
      ];

  const path = currentPath();
  for (const [href, label, extraClass] of links) {
    const link = document.createElement("a");
    link.href = `#${href}`;
    link.className = [href === path ? "is-active" : "", extraClass ?? ""].join(" ").trim();
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
    ++renderGeneration;
    setPartnerPortalLabel();
    logout();
    location.hash = "#/login";
  });
  menu.querySelector('[data-action="profile"]').addEventListener("click", () => {
    closeProfileMenu();
    location.hash = session.isCompany ? "#/company/profile" : "#/partner/profile";
  });

  window.lucide?.createIcons();
  refreshOrdersBadge();
  refreshInquiriesBadge();
}

function setPartnerPortalLabel(name) {
  appRole.textContent = name ? `Partnerportal der Firma ${name}` : "Partnerportal";
}

function showPartnerPortalRetry() {
  setPartnerPortalLabel();
  const retry = document.createElement("button");
  retry.type = "button";
  retry.className = "portal-label-retry";
  retry.textContent = "Name nicht verfügbar – erneut laden";
  retry.addEventListener("click", () => loadPartnerPortalLabel(renderGeneration));
  appRole.appendChild(retry);
}

async function loadPartnerPortalLabel(generation) {
  const session = await ensureValidSession();
  if (!session?.isPartner || generation !== renderGeneration) return;
  const accessToken = session.accessToken;
  try {
    const profile = await apiFetch("/partner/profile");
    const current = getSession();
    if (generation !== renderGeneration || !current?.isPartner || current.accessToken !== accessToken) return;
    setPartnerPortalLabel(profile.name);
  } catch {
    const current = getSession();
    if (generation === renderGeneration && current?.isPartner && current.accessToken === accessToken) showPartnerPortalRetry();
  }
}

// Unread-order count for the company sidebar "Bestellungen" nav item
// (order-notifications spec). Fetched from a dedicated count endpoint (not the full
// order list, which this runs on every nav re-render) — no polling/websocket, per design.md.
export async function refreshOrdersBadge() {
  const session = getSession();
  if (!session?.isCompany) return;
  await refreshNavBadge("#/company/orders", "/company/orders/unseen-count", "neue Bestellungen");
}

// Unread shop inquiries on "Anfragen" (shop-inquiries spec), same pattern as above.
export async function refreshInquiriesBadge() {
  const session = getSession();
  if (!session?.isPartner) return;
  await refreshNavBadge("#/partner/shop/inquiries", "/partner/shop/inquiries/unread-count", "ungelesene Anfragen");
}

async function refreshNavBadge(href, countPath, srLabel) {
  const link = appNav.querySelector(`a[href="${href}"]`);
  if (!link) return;
  try {
    const count = await apiFetch(countPath);
    let badge = link.querySelector(".nav-badge");
    if (count > 0) {
      if (!badge) {
        badge = document.createElement("span");
        badge.className = "nav-badge";
        link.appendChild(badge);
      }
      badge.textContent = count > 99 ? "99+" : String(count);
      badge.setAttribute("aria-label", `${count} ${srLabel}`);
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
  setPartnerPortalLabel();
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
  appShell.classList.toggle("app-shell--fullscreen", Boolean(route.fullscreen));
  appRoot.innerHTML = "";
  appRoot.appendChild(document.createElement(route.tag));
  renderNav();
  if (route.role === "partner") loadPartnerPortalLabel(myGeneration);
}

window.addEventListener("DOMContentLoaded", async () => {
  await import("../components/index.js");
  import("../pages/login-page.js");
  import("../pages/company-dashboard-page.js");
  import("../pages/company-profile-page.js");
  import("../pages/company-product-list-page.js");
  import("../pages/company-category-list-page.js");
  import("../pages/company-partner-list-page.js");
  import("../pages/company-order-list-page.js");
  import("../pages/company-import-page.js");
  import("../pages/partner-dashboard-page.js");
  import("../pages/partner-product-list-page.js");
  import("../pages/partner-order-list-page.js");
  import("../pages/partner-order-new-page.js");
  import("../pages/partner-customer-list-page.js");
  import("../pages/partner-profile-page.js");
  import("../pages/partner-shop-page.js");
  import("../pages/partner-shop-inquiries-page.js");
  import("../pages/partner-shop-editor-page.js");
  window.addEventListener("hashchange", render);
  render();
});
