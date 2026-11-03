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
const WIDTH = 1280;
const HEIGHT = 800;

// Only demo data may end up on the public page: refuse anything but a local environment.
if (!/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(BASE)) {
  console.error(`Abbruch: Screenshots nur von einer lokalen Umgebung mit Demo-Daten (FRONTEND_URL=${BASE}).`);
  process.exit(1);
}

// Demo logins from DemoDataSeeder / the dev realm (docs/SETUP.md), never real accounts.
const VIEWS = [
  // Dashboards on "Dieses Jahr": the current month of the demo data may still be empty.
  { name: "company-dashboard", user: "company-demo", hash: "#/company/dashboard", year: true },
  { name: "company-orders", user: "company-demo", hash: "#/company/orders" },
  { name: "partner-dashboard", user: "partner-demo", hash: "#/partner/dashboard", year: true },
  { name: "partner-order-new", user: "partner-demo", hash: "#/partner/orders/new" },
  { name: "shop", user: "partner-demo", shop: true },
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
  const context = await browser.newContext({ viewport: { width: WIDTH, height: HEIGHT }, deviceScaleFactor: 1, locale: "de-AT" });
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
  const base64 = await page.evaluate(async ({ png, width, height }) => {
    const image = new Image();
    image.src = `data:image/png;base64,${png}`;
    await image.decode();
    const canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    canvas.getContext("2d").drawImage(image, 0, 0);
    return canvas.toDataURL("image/webp", 0.86).split(",")[1];
  }, { png: png.toString("base64"), width: WIDTH, height: HEIGHT });
  return Buffer.from(base64, "base64");
}

const pages = new Map();
for (const view of VIEWS.filter((candidate) => !wanted.length || wanted.includes(candidate.name))) {
  if (!pages.has(view.user)) pages.set(view.user, await loggedInPage(view.user));
  const page = pages.get(view.user);
  if (view.shop) {
    await page.goto(`${BASE}/portal/#/partner/shop`);
    await page.waitForFunction(() => document.querySelector("#shop-kennung")?.textContent.trim());
    const kennung = (await page.textContent("#shop-kennung")).trim();
    await page.goto(`${BASE}/shop/${encodeURIComponent(kennung)}`);
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
      await page.mouse.click(5, HEIGHT - 5);
    }
  }
  await settle(page);
  const png = await page.screenshot({ clip: { x: 0, y: 0, width: WIDTH, height: HEIGHT } });
  await writeFile(new URL(`${view.name}.webp`, OUT), await toWebp(page, png));
  // Provenance sidecar, same convention as the shop template images.
  const origin = view.shop ? "public demo shop of partner-demo" : `portal view ${view.hash} as ${view.user}`;
  const photos = view.shop ? "; shows CC0 shop template photos (backend/src/main/resources/shop-templates/images)" : "";
  await writeFile(new URL(`${view.name}.webp.json`, OUT), `${JSON.stringify({
    prompt: `Origin: screenshot of the local PAPO demo data (DemoDataSeeder), ${origin}, ${WIDTH}x${HEIGHT}, by tools/render_landing_screenshots.mjs${photos}`,
    createdAt: new Date().toISOString(),
  }, null, 2)}\n`);
  console.log(`${view.name}.webp geschrieben`);
}
await browser.close();
