// Public shop bootstrap. Must never import portal modules (src/lib/auth.js, api.js,
// router.js, pages/*): the shop runs unauthenticated and only calls /api/shop/<kennung>.
// Allowed lib modules have no imports of their own: config.js, html.js, format.js, money.js.
import { escapeHtml } from "../lib/html.js";
import { createPreviewApi, createShopApi } from "./shop-api.js";
import { applyTheme } from "./shop-theme.js";
import { CART_EVENT, cartCount } from "./shop-cart.js";
import "./shop-page-view.js";

const NOT_REACHABLE = "Dieser Shop ist derzeit nicht erreichbar.";
const LEGAL_PENDING = "Dieser Rechtstext wird gerade vorbereitet.";

// Every shop lives under /shop/<kennung>; Caddy (prod) and tools/dev_frontend_server.py (dev)
// answer all of these paths with shop.html:
//   /shop/<kennung>              Startseite
//   /shop/<kennung>/<segment>    other online page (or a legal system page)
//   /shop/<kennung>/artikel/<id> Artikeldetail for one article
//   /shop/<kennung>/warenkorb    cart, /shop/<kennung>/kasse checkout (reserved system segments)
function readRoute() {
  const match = location.pathname.match(/^\/shop\/([^/]+)(?:\/([^/]+))?(?:\/([^/]+))?\/?$/);
  if (!match) return null;
  try {
    const [kennung, segment, articleId] = match.slice(1).map((part) => (part ? decodeURIComponent(part) : ""));
    // /shop/<k>/startseite is the same page as the shop root.
    return { kennung, segment: segment === "startseite" ? "" : segment, articleId };
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
        ${api.live ? cartLinkHtml(api, currentPath) : ""}
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

// Cart in the header of the live shop only (preview and template demo have no cart).
function cartLinkHtml(api, currentPath) {
  const current = currentPath === "warenkorb" ? ' aria-current="page"' : "";
  return `<a class="shop-cartlink" href="${escapeHtml(api.href("warenkorb"))}"${current} data-cartlink>
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><path d="M3 6h18M16 10a4 4 0 0 1-8 0"/></svg>
    <span class="shop-cartlink__label">Warenkorb</span>
    <span class="shop-cartlink__count" data-cartcount hidden></span>
  </a>`;
}

function updateCartLink(kennung, { bump = false } = {}) {
  const link = root.querySelector("[data-cartlink]");
  if (!link) return;
  const count = cartCount(kennung);
  const badge = link.querySelector("[data-cartcount]");
  badge.textContent = count > 99 ? "99+" : String(count);
  badge.hidden = count === 0;
  link.setAttribute("aria-label", count ? `Warenkorb, ${count} Artikel` : "Warenkorb, leer");
  if (bump && count) {
    link.classList.remove("shop-cartlink--bump");
    void link.offsetWidth; // restart the animation on repeated adds
    link.classList.add("shop-cartlink--bump");
  }
}

function renderMessage(main, text) {
  main.innerHTML = `<section class="shop-block shop-block--spacing-large"><div class="shop-inner shop-align-center">
    <p class="shop-text">${escapeHtml(text)}</p></div></section>`;
}

// Editor preview (shop-builder spec): /shop.html?preview inside the editor's iframe. The page
// renders whatever the embedding editor sends; only messages from the same-origin parent count.
const PREVIEW_DATA = "papo-shop-preview";

// Editing tools inside the preview (shop-preview-tools.js), never loaded by the public shop.
let previewTools = null;

async function startPreview() {
  const { installPreviewTools } = await import("./shop-preview-tools.js");
  previewTools = installPreviewTools(root, (message) => window.parent.postMessage(message, location.origin));
  window.addEventListener("message", (event) => {
    if (event.origin !== location.origin || event.source !== window.parent) return;
    if (event.data?.type === PREVIEW_DATA) renderPreview(event.data.payload);
    else if (event.data?.type === "papo-shop-focus") previewTools?.focusBlock(event.data.blockId);
  });
  // Links lead nowhere in the preview; a click on a block selects it in the editor instead.
  root.addEventListener("click", (event) => {
    if (event.target.closest("a")) event.preventDefault();
    const block = event.target.closest("[data-block-id]");
    if (block) window.parent.postMessage({ type: "papo-shop-select", blockId: block.dataset.blockId }, location.origin);
  });
  window.parent.postMessage({ type: "papo-shop-preview-ready" }, location.origin);
}

// Last preview frame: layout and block context are reused while they stay the same, so an
// edit only re-renders the changed blocks.
let previewState = null;

function renderPreview(data) {
  const api = createPreviewApi(data);
  applyTheme(document.documentElement, data.theme);
  document.title = `Vorschau: ${data.page.menuName}`;
  // bare: the blocks alone, without header and footer (palette preview of one block).
  if (data.bare) {
    previewState = null;
    root.className = "shop-root shop-root--bare shop-root--preview";
    root.innerHTML = `<main class="shop-main"></main>`;
    const view = document.createElement("shop-page-view");
    view.data = { blocks: data.page.blocks, context: { api, article: data.article, shopName: data.shop.displayName } };
    root.querySelector("main").append(view);
    // Lets the editor measure the drawn block (palette hover preview).
    window.parent.postMessage({ type: "papo-shop-rendered" }, location.origin);
    return;
  }
  const layoutKey = JSON.stringify([data.shop, data.navigation, data.page.path]);
  const contextKey = JSON.stringify([data.assetUrls, data.imageUrls, data.article?.id ?? null, data.shop.displayName,
    data.articles.map((article) => article.id)]);
  if (previewState?.layoutKey !== layoutKey) {
    const scrollY = window.scrollY;
    const main = renderLayout(api, data.shop, data.navigation, data.page.path);
    root.classList.add("shop-root--preview");
    const view = document.createElement("shop-page-view");
    main.append(view);
    previewState = { layoutKey, contextKey: null, context: null, view };
    window.scrollTo(0, scrollY);
  }
  if (previewState.contextKey !== contextKey) {
    previewState.contextKey = contextKey;
    previewState.context = { api, article: data.article, shopName: data.shop.displayName };
  }
  const { view } = previewState;
  view.update({ blocks: data.page.blocks, context: previewState.context });
  view.querySelectorAll(".shop-block--selected").forEach((element) => element.classList.remove("shop-block--selected"));
  if (data.selectedBlockId) {
    view.querySelector(`[data-block-id="${CSS.escape(data.selectedBlockId)}"]`)?.classList.add("shop-block--selected");
  }
  previewTools?.afterRender(view, data.selectedBlockId, data.markers);
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
  updateCartLink(api.kennung);
  window.addEventListener(CART_EVENT, (event) => {
    if (event.detail?.kennung === api.kennung) updateCartLink(api.kennung, { bump: event.detail.added });
  });
  window.addEventListener("storage", () => updateCartLink(api.kennung));

  if ((route.segment === "warenkorb" || route.segment === "kasse") && !route.articleId) {
    const { renderCartPage, renderCheckoutPage } = await import("./shop-checkout.js");
    const render = route.segment === "warenkorb" ? renderCartPage : renderCheckoutPage;
    await render(main, { api, shop, navigation });
    return;
  }

  const legalPage = navigation.legal.find((item) => item.segment === route.segment);
  if (legalPage && !route.articleId) {
    document.title = `${legalPage.menuName} – ${shop.displayName}`;
    renderMessage(main, LEGAL_PENDING);
    return;
  }

  try {
    // The article-detail template only exists under /artikel/<id>, never by its own segment.
    if ((route.articleId && !isDetail) || route.segment === "artikeldetail") {
      throw Object.assign(new Error("not found"), { status: 404 });
    }
    const segment = isDetail ? "artikeldetail" : route.segment || "startseite";
    const [page, article] = await Promise.all([
      api.page(segment),
      isDetail ? api.article(route.articleId).catch(() => null) : null,
    ]);
    document.title = article ? `${article.name} – ${shop.displayName}` : page.seoTitle || `${page.menuName} – ${shop.displayName}`;
    if (page.seoDescription && !article) {
      const meta = document.createElement("meta");
      meta.name = "description";
      meta.content = page.seoDescription;
      document.head.append(meta);
    }
    const view = document.createElement("shop-page-view");
    view.data = { blocks: page.blocks, context: { api, article, shopName: shop.displayName } };
    main.append(view);
  } catch {
    document.title = shop.displayName;
    renderMessage(main, "Diese Seite gibt es nicht.");
  }
}

// Demo shop (shop-templates spec "Demo shop per template"): /shop.html?demo in the editor's
// template viewer. The editor sends whole pages keyed by path ("" Startseite, "artikel",
// "artikeldetail", or a free page); links switch between them here. Nothing is editable and
// nothing is requested from the server.
const DEMO_DATA = "papo-shop-demo";

function startDemo() {
  let demo = null;
  window.addEventListener("message", (event) => {
    if (event.origin !== location.origin || event.source !== window.parent) return;
    if (event.data?.type !== DEMO_DATA) return;
    demo = event.data.payload;
    showDemo(demo.start ?? "");
  });
  root.addEventListener("click", (event) => {
    const link = event.target.closest('a[href^="#"]');
    if (!link || !demo) return;
    event.preventDefault();
    showDemo(link.getAttribute("href").slice(1));
  });

  function showDemo(path) {
    const [segment, articleId] = path.split("/");
    const isDetail = segment === "artikel" && articleId !== undefined && demo.pages.artikeldetail;
    const key = isDetail ? "artikeldetail" : segment in demo.pages ? segment : Object.keys(demo.pages)[0];
    const article = isDetail
      ? demo.articles.find((candidate) => String(candidate.id) === articleId) ?? demo.articles[0] ?? null
      : null;
    const data = { ...demo, article };
    const api = { ...createPreviewApi(data), editing: false };
    applyTheme(document.documentElement, demo.theme);
    document.title = article ? `${article.name} – ${demo.shop.displayName}` : demo.shop.displayName;
    const main = renderLayout(api, demo.shop, demo.navigation, isDetail ? "artikel" : key);
    const view = document.createElement("shop-page-view");
    view.data = { blocks: demo.pages[key].blocks, context: { api, article, shopName: demo.shop.displayName } };
    main.append(view);
    window.scrollTo(0, 0);
  }

  window.parent.postMessage({ type: "papo-shop-demo-ready" }, location.origin);
}

const mode = new URLSearchParams(location.search);
if (mode.has("preview")) {
  startPreview();
} else if (mode.has("demo")) {
  startDemo();
} else {
  start();
}
