// Public shop bootstrap. Must never import portal modules (src/lib/auth.js, api.js,
// router.js, pages/*): the shop runs unauthenticated and only calls /api/shop/<kennung>.

const NOT_REACHABLE = "Dieser Shop ist derzeit nicht erreichbar.";

// Same environment split as the portal's config.js, kept separate on purpose.
const isLocalDev = location.hostname === "localhost" && location.port === "3000";
const API_BASE = isLocalDev ? "http://localhost:8080/api" : `${location.origin}/api`;

// Every shop lives at /shop/<kennung>; Caddy (prod) and tools/dev_frontend_server.py (dev)
// both answer that path with shop.html.
function readKennung() {
  const match = location.pathname.match(/^\/shop\/([^/]+)\/?$/);
  return match ? decodeURIComponent(match[1]) : "";
}

const root = document.getElementById("shop-root");

function renderUnavailable() {
  document.title = "Shop nicht erreichbar";
  root.innerHTML = "";
  const box = document.createElement("section");
  box.className = "shop-notice";
  const heading = document.createElement("h1");
  heading.textContent = NOT_REACHABLE;
  box.append(heading);
  root.append(box);
}

function renderShop(shop) {
  document.title = shop.displayName;
  root.innerHTML = "";
  const box = document.createElement("section");
  box.className = "shop-notice";
  const heading = document.createElement("h1");
  heading.textContent = shop.displayName;
  const text = document.createElement("p");
  text.textContent = "Dieser Shop wird gerade eingerichtet.";
  box.append(heading, text);
  root.append(box);
}

async function start() {
  const kennung = readKennung();
  if (!/^[a-z0-9-]{3,40}$/.test(kennung)) {
    renderUnavailable();
    return;
  }
  try {
    const response = await fetch(`${API_BASE}/shop/${encodeURIComponent(kennung)}`, {
      credentials: "omit",
    });
    if (!response.ok) {
      renderUnavailable();
      return;
    }
    renderShop(await response.json());
  } catch {
    renderUnavailable();
  }
}

start();
