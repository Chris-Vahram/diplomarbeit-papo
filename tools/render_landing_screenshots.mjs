// Renders the real screenshots of the public landing page (add-public-landing-page
// design.md Decision 8) from the running local application with its demo data.
//
// Needs the local dev environment (docs/SETUP.md: docker compose, backend in dev mode with
// DemoDataSeeder, `python3 tools/dev_frontend_server.py` on port 3000) and a local
// Playwright; it is not a project dependency:
//   npm i --prefix /tmp/pw playwright-core
//   PLAYWRIGHT_MODULE=/tmp/pw/node_modules/playwright-core/index.mjs \
//     CHROMIUM=<path to a Chromium binary> node tools/render_landing_screenshots.mjs [name ...]
// Without names every view below is rendered to frontend/assets/landing/<name>.webp.
import { writeFile, mkdir } from "node:fs/promises";

const ROOT = new URL("../", import.meta.url);
const OUT = new URL("frontend/assets/landing/", ROOT);
const BASE = process.env.FRONTEND_URL ?? "http://localhost:3000";
// The landing shows each image only about 330-650px wide, so UI text must stay legible: narrow
// viewports (the portal sidebar becomes a drawer below 62em) at device scale 2, cropped to the
// content below the topbar or from a given element (`from`).
const WIDTH = 900;
const HEIGHT = 560;
const TOP = 64;
const SCALE = 2;

// Only demo data may end up on the public page: refuse anything but a local environment.
if (!/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(BASE)) {
  console.error(`Abbruch: Screenshots nur von einer lokalen Umgebung mit Demo-Daten (FRONTEND_URL=${BASE}).`);
  process.exit(1);
}

// Demo logins from DemoDataSeeder / the dev realm (docs/SETUP.md), never real accounts.
// The landing compares company and partner side by side, so each pair shares one size.
const VIEWS = [
  // Dashboards on "Dieses Jahr": the current month of the demo data may still be empty.
  { name: "company-dashboard", user: "company-demo", hash: "#/company/dashboard", year: true },
  { name: "company-orders", user: "company-demo", hash: "#/company/orders", from: "table" },
  { name: "partner-dashboard", user: "partner-demo", hash: "#/partner/dashboard", year: true },
  { name: "partner-order-new", user: "partner-demo", hash: "#/partner/orders/new" },
  // Shop builder (full-screen editor with live preview) and the public article page.
  { name: "shop-editor", user: "partner-demo", hash: "#/partner/shop/editor", fullscreen: true, width: 1280, height: 760, wait: 2500 },
  { name: "shop-article", user: "partner-demo", shop: "artikel/1", width: 900, height: 560 },
];

const { chromium } = await import(process.env.PLAYWRIGHT_MODULE ?? "playwright-core");
const wanted = process.argv.slice(2);
const browser = await chromium.launch({ executablePath: process.env.CHROMIUM });
await mkdir(OUT, { recursive: true });

async function settle(page) {
  await page.waitForLoadState("networkidle");
  await page.evaluate(() => document.fonts.ready);
  // Lets entrance transitions and lazy images finish.
  await page.waitForTimeout(800);
}

async function loggedInPage(user) {
  const context = await browser.newContext({ viewport: { width: WIDTH, height: HEIGHT + TOP }, deviceScaleFactor: SCALE, locale: "de-AT" });
  const page = await context.newPage();
  await page.goto(`${BASE}/portal/#/login`);
  await page.fill('input[name="username"]', user);
  await page.fill('input[name="password"]', user);
  await page.click('.login-form button[type="submit"]');
  await page.waitForFunction(() => /dashboard/.test(location.hash));
  return page;
}

async function toWebp(page, png) {
  // Encode in the browser: Chromium writes WebP, so no image tool is needed.
  const base64 = await page.evaluate(async ({ png }) => {
    const image = new Image();
    image.src = `data:image/png;base64,${png}`;
    await image.decode();
    const canvas = document.createElement("canvas");
    canvas.width = image.naturalWidth;
    canvas.height = image.naturalHeight;
    canvas.getContext("2d").drawImage(image, 0, 0);
    return canvas.toDataURL("image/webp", 0.86).split(",")[1];
  }, { png: png.toString("base64") });
  return Buffer.from(base64, "base64");
}

const pages = new Map();
for (const view of VIEWS.filter((candidate) => !wanted.length || wanted.includes(candidate.name))) {
  if (!pages.has(view.user)) pages.set(view.user, await loggedInPage(view.user));
  const page = pages.get(view.user);
  const width = view.width ?? WIDTH;
  const height = view.height ?? HEIGHT;
  await page.setViewportSize({ width, height: height + TOP });
  if (view.shop) {
    await page.goto(`${BASE}/portal/#/partner/shop`);
    await page.waitForFunction(() => document.querySelector("#shop-kennung")?.textContent.trim());
    const kennung = (await page.textContent("#shop-kennung")).trim();
    await page.goto(`${BASE}/shop/${encodeURIComponent(kennung)}/${view.shop}`);
    await settle(page);
    // The consent banner would cover the shop: answer it like a visitor would.
    const necessary = page.locator('.shop-consent__choice[data-choice="necessary"]').first();
    if (await necessary.isVisible()) await necessary.click();
  } else {
    await page.goto(`${BASE}/portal/${view.hash}`);
    await settle(page);
    if (view.year) {
      await page.click("#period-button");
      await page.click('[data-preset="year"]');
      await page.keyboard.press("Escape");
      await page.mouse.click(5, height - 5);
    }
  }
  await settle(page);
  if (view.wait) await page.waitForTimeout(view.wait);
  await page.evaluate(() => Promise.all([...document.images].map((image) => image.decode().catch(() => {}))));
  // Keep the pointer off rows and buttons so no hover state ends up in the picture.
  await page.mouse.move(width / 2, 1);
  // Shop and full-screen editor have no portal topbar; other views drop it.
  let top = view.shop || view.fullscreen ? 0 : TOP;
  if (view.from) {
    await page.evaluate((selector) => {
      window.scrollTo(0, document.querySelector(selector).getBoundingClientRect().top + window.scrollY - 16);
    }, view.from);
    await page.waitForTimeout(200);
    top = 0;
  }
  const png = await page.screenshot({ clip: { x: 0, y: top, width, height } });
  await writeFile(new URL(`${view.name}.webp`, OUT), await toWebp(page, png));
  // Provenance sidecar, same convention as the shop template images.
  const origin = view.shop ? `public demo shop of partner-demo, page "${view.shop}"` : `portal view ${view.hash} as ${view.user}`;
  const photos = view.shop || view.fullscreen ? "; shows CC0 shop template photos (backend/src/main/resources/shop-templates/images)" : "";
  await writeFile(new URL(`${view.name}.webp.json`, OUT), `${JSON.stringify({
    prompt: `Origin: screenshot of the local PAPO demo data (DemoDataSeeder), ${origin}, ${width}x${height} CSS px at scale ${SCALE}, by tools/render_landing_screenshots.mjs${photos}`,
    createdAt: new Date().toISOString(),
  }, null, 2)}\n`);
  console.log(`${view.name}.webp geschrieben`);
}
await browser.close();
