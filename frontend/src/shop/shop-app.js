// Public shop bootstrap. Must never import portal modules (src/lib/auth.js, api.js,
// router.js, pages/*): the shop runs unauthenticated and only calls /api/shop/<kennung>.
// Allowed lib modules have no imports of their own: config.js, html.js, format.js.
import { escapeHtml } from "../lib/html.js";
import { createShopApi } from "./shop-api.js";
import { applyTheme } from "./shop-theme.js";
import "./shop-page-view.js";

const NOT_REACHABLE = "Dieser Shop ist derzeit nicht erreichbar.";
const LEGAL_PENDING = "Dieser Rechtstext wird gerade vorbereitet.";

// Every shop lives under /shop/<kennung>; Caddy (prod) and tools/dev_frontend_server.py (dev)
// answer all of these paths with shop.html:
//   /shop/<kennung>              Startseite
//   /shop/<kennung>/<segment>    other online page (or a legal system page)
//   /shop/<kennung>/artikel/<id> Artikeldetail for one article
function readRoute() {
  const match = location.pathname.match(/^\/shop\/([^/]+)(?:\/([^/]+))?(?:\/([^/]+))?\/?$/);
  if (!match) return null;
  try {
    const [kennung, segment, articleId] = match.slice(1).map((part) => (part ? decodeURIComponent(part) : ""));
    return { kennung, segment, articleId };
  } catch {
    return null; // malformed escape such as /shop/%E0 -> neutral "nicht erreichbar" page
  }
}

const root = document.getElementById("shop-root");

function renderUnavailable() {
  document.title = "Shop nicht erreichbar";
  root.className = "shop-root shop-root--notice";
  root.innerHTML = `<section class="shop-notice"><h1>${escapeHtml(NOT_REACHABLE)}</h1></section>`;
}

function renderLayout(api, shop, navigation, currentPath) {
  const links = navigation.items
    .map((item) => {
      const current = item.path === currentPath ? ' aria-current="page"' : "";
      return `<li><a href="${escapeHtml(api.href(item.path))}"${current}>${escapeHtml(item.menuName)}</a></li>`;
    })
    .join("");
  const legal = navigation.legal
    .map((item) => `<li><a href="${escapeHtml(api.href(item.path))}">${escapeHtml(item.menuName)}</a></li>`)
    .join("");
  root.className = "shop-root";
  root.innerHTML = `
    <header class="shop-header">
      <div class="shop-header__inner">
        <a class="shop-header__name" href="${escapeHtml(api.href())}">${escapeHtml(shop.displayName)}</a>
        <nav class="shop-nav" aria-label="Shop-Navigation"><ul>${links}</ul></nav>
      </div>
    </header>
    <main class="shop-main" id="shop-main"></main>
    <footer class="shop-footer">
      <div class="shop-footer__inner">
        <nav aria-label="Rechtliches"><ul class="shop-footer__links">${legal}</ul></nav>
        ${navigation.sellerName ? `<p>Verkauf durch ${escapeHtml(navigation.sellerName)}</p>` : ""}
      </div>
    </footer>`;
  return root.querySelector("#shop-main");
}

function renderMessage(main, text) {
  main.innerHTML = `<section class="shop-block shop-block--spacing-large"><div class="shop-inner shop-align-center">
    <p class="shop-text">${escapeHtml(text)}</p></div></section>`;
}

async function start() {
  const route = readRoute();
  if (!route || !/^[a-z0-9-]{3,40}$/.test(route.kennung)) {
    renderUnavailable();
    return;
  }
  const api = createShopApi(route.kennung);
  let shop, theme, navigation;
  try {
    [shop, theme, navigation] = await Promise.all([api.shop(), api.theme(), api.navigation()]);
  } catch {
    renderUnavailable();
    return;
  }

  applyTheme(document.documentElement, theme);
  const isDetail = route.segment === "artikel" && route.articleId;
  const main = renderLayout(api, shop, navigation, isDetail ? "artikel" : route.segment);

  const legalPage = navigation.legal.find((item) => item.segment === route.segment);
  if (legalPage && !route.articleId) {
    document.title = `${legalPage.menuName} – ${shop.displayName}`;
    renderMessage(main, LEGAL_PENDING);
    return;
  }

  try {
    const segment = isDetail ? "artikeldetail" : route.segment || "startseite";
    if (route.articleId && !isDetail) throw Object.assign(new Error("not found"), { status: 404 });
    const [page, article] = await Promise.all([
      api.page(segment),
      isDetail ? api.article(route.articleId).catch(() => null) : null,
    ]);
    document.title = `${article?.name ?? page.menuName} – ${shop.displayName}`;
    const view = document.createElement("shop-page-view");
    view.data = { blocks: page.blocks, context: { api, article } };
    main.append(view);
  } catch {
    document.title = shop.displayName;
    renderMessage(main, "Diese Seite gibt es nicht.");
  }
}

start();
