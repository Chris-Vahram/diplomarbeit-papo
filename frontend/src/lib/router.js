import { isAuthenticated, hasRole, getSession, logout } from "./auth.js";

// Route table: hash path -> { tag, role, title }
// role: null = public, "company" | "partner" = required realm role for FR-20 deep-link protection.
const routes = {
  "/login": { tag: "login-page", role: null, title: "Anmeldung" },
  "/company/dashboard": { tag: "company-dashboard-page", role: "company", title: "Firmen-Dashboard" },
  "/company/products": { tag: "company-product-list-page", role: "company", title: "Produkte" },
  "/company/partners": { tag: "company-partner-list-page", role: "company", title: "Partner" },
  "/company/orders": { tag: "company-order-list-page", role: "company", title: "Bestellungen" },
  "/company/import": { tag: "company-import-page", role: "company", title: "Datenimport" },
  "/partner/dashboard": { tag: "partner-dashboard-page", role: "partner", title: "Partner-Dashboard" },
  "/partner/products": { tag: "partner-product-list-page", role: "partner", title: "Produkte" },
  "/partner/orders": { tag: "partner-order-list-page", role: "partner", title: "Meine Bestellungen" },
  "/partner/orders/new": { tag: "partner-order-new-page", role: "partner", title: "Neue Bestellung" },
  "/partner/profile": { tag: "partner-profile-page", role: "partner", title: "Mein Profil" },
};

const appRoot = document.getElementById("app-root");
const appNav = document.getElementById("app-nav");
const appUser = document.getElementById("app-user");

function currentPath() {
  return location.hash.replace(/^#/, "") || "/login";
}

function resolveDefaultRoute() {
  const session = getSession();
  if (!session) return "/login";
  if (session.isCompany) return "/company/dashboard";
  return "/partner/dashboard";
}

function renderNav() {
  const session = getSession();
  appNav.innerHTML = "";
  appUser.innerHTML = "";

  if (!session) return;

  const links = session.isCompany
    ? [
        ["#/company/dashboard", "Dashboard"],
        ["#/company/products", "Produkte"],
        ["#/company/partners", "Partner"],
        ["#/company/orders", "Bestellungen"],
        ["#/company/import", "Datenimport"],
      ]
    : [
        ["#/partner/dashboard", "Dashboard"],
        ["#/partner/products", "Produkte"],
        ["#/partner/orders", "Meine Bestellungen"],
        ["#/partner/profile", "Mein Profil"],
      ];

  for (const [href, label] of links) {
    const link = document.createElement("a");
    link.href = href;
    link.textContent = label;
    appNav.appendChild(link);
  }

  const userLabel = document.createElement("span");
  userLabel.textContent = `${session.username} (${session.roles.join(", ")})`;
  appUser.appendChild(userLabel);

  const logoutButton = document.createElement("button");
  logoutButton.textContent = "Abmelden";
  logoutButton.addEventListener("click", () => {
    logout();
    location.hash = "#/login";
  });
  appUser.appendChild(logoutButton);
}

function render() {
  const path = currentPath();
  const route = routes[path];

  if (!route) {
    location.hash = `#${resolveDefaultRoute()}`;
    return;
  }

  // FR-20: protected views must not be reachable via direct URL without authorization.
  if (route.role && !isAuthenticated()) {
    location.hash = "#/login";
    return;
  }
  if (route.role && !hasRole(route.role)) {
    location.hash = `#${resolveDefaultRoute()}`;
    return;
  }
  if (path === "/login" && isAuthenticated()) {
    location.hash = `#${resolveDefaultRoute()}`;
    return;
  }

  document.title = `Partnerportal – ${route.title}`;
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
