// Renders the WebP thumbnails of the shop template catalogue (add-shop-templates design.md
// Decision 2) with the real shop renderer in its editor-preview mode.
//
// Needs the static frontend server (`python3 tools/dev_frontend_server.py`, port 3000) and a
// local Playwright; it is not a project dependency:
//   npm i --prefix /tmp/pw playwright-core
//   PLAYWRIGHT_MODULE=/tmp/pw/node_modules/playwright-core/index.mjs \
//     CHROMIUM=<path to a Chromium binary> node tools/render_shop_template_thumbnails.mjs [id ...]
// Without ids every template in backend/src/main/resources/shop-templates/index.json is rendered.
import { readFile, writeFile } from "node:fs/promises";

const ROOT = new URL("../", import.meta.url);
const TEMPLATES = new URL("backend/src/main/resources/shop-templates/", ROOT);
const OUT = new URL("frontend/assets/shop-templates/", ROOT);
const BASE = process.env.FRONTEND_URL ?? "http://localhost:3000";
const WIDTH = 1280;
const HEIGHT = 800;

const { chromium } = await import(process.env.PLAYWRIGHT_MODULE ?? "playwright-core");
const json = async (name) => JSON.parse(await readFile(new URL(name, TEMPLATES), "utf8"));

// Neutral sample catalogue: the bundled article placeholder graphics, no real data.
const SAMPLE = [
  ["Akku-Bohrschrauber", "Werkzeug", "werkzeug", "189.90"],
  ["Fassadenfarbe Weiß, 10 l", "Malerbedarf", "malerbedarf", "74.50"],
  ["Schutzhandschuhe, 10 Paar", "Arbeitsschutz", "arbeitsschutz", "24.90"],
  ["Installationskabel, 50 m", "Elektro", "elektro", "59.00"],
  ["Fliesenkleber, 25 kg", "Baustoffe", "baustoffe", "18.40"],
  ["Wasserwaage 60 cm", "Werkzeug", "werkzeug", "32.90"],
  ["Abdeckvlies, 25 m²", "Malerbedarf", "malerbedarf", "39.90"],
  ["Gehörschutz", "Arbeitsschutz", "arbeitsschutz", "21.50"],
].map(([name, category, image, price], index) => ({
  id: index + 1,
  name,
  description: "Beispielartikel für die Vorschau der Vorlage.",
  categoryId: null,
  category,
  imageUrl: `/assets/products/${image}.svg`,
  grossPrice: price,
  taxRatePercent: 20,
}));

const PATHS = { startseite: "", artikel: "artikel", artikeldetail: "artikel", frei: "ueber-uns" };
const NAMES = { startseite: "Startseite", artikel: "Artikel", artikeldetail: "Artikeldetail" };

function payload(entry, template, theme) {
  const items = [
    { segment: "startseite", menuName: "Startseite", path: "" },
    { segment: "artikel", menuName: "Artikel", path: "artikel" },
    { segment: "kontakt", menuName: "Kontakt", path: "kontakt" },
  ];
  return {
    shop: { kennung: "beispiel", displayName: "Beispielbetrieb", reachable: true },
    theme,
    navigation: { items, legal: [], sellerName: null },
    page: {
      menuName: NAMES[entry.page] ?? entry.name,
      path: PATHS[entry.page],
      // An empty "Hervorgehobene Artikel" block shows sample articles, like the editor's block preview.
      blocks: template.blocks.map((block, index) => ({
        id: `block-${index}`,
        ...block,
        config: block.type === "featured-articles" && !block.config.articleIds?.length
          ? { ...block.config, articleIds: [1, 2, 3] }
          : block.config,
      })),
    },
    article: entry.page === "artikeldetail" ? SAMPLE[0] : null,
    articles: SAMPLE,
    assetUrls: {},
    imageUrls: {},
    selectedBlockId: null,
  };
}

const index = await json("index.json");
const wanted = process.argv.slice(2);
const defaultTheme = await json("theme.json");
const browser = await chromium.launch({ executablePath: process.env.CHROMIUM });
const page = await browser.newPage({ viewport: { width: WIDTH, height: HEIGHT }, deviceScaleFactor: 1 });
// Any same-origin page can host the preview frame; shop.html?preview only trusts its parent.
await page.goto(`${BASE}/shop.html`);
for (const entry of index.filter((candidate) => !wanted.length || wanted.includes(candidate.id))) {
  const template = await json(`${entry.id}.json`);
  const data = payload(entry, template, template.theme ?? defaultTheme);
  await page.evaluate(async ({ data, width, height }) => {
    document.body.innerHTML = "";
    const frame = document.createElement("iframe");
    frame.style.cssText = `position:fixed;inset:0;width:${width}px;height:${height}px;border:0`;
    const ready = new Promise((resolve) => {
      window.addEventListener("message", function onReady(event) {
        if (event.data?.type !== "papo-shop-preview-ready") return;
        window.removeEventListener("message", onReady);
        resolve();
      });
    });
    frame.src = "/shop.html?preview=1";
    document.body.append(frame);
    await ready;
    frame.contentWindow.postMessage({ type: "papo-shop-preview", payload: data }, location.origin);
    await new Promise((resolve) => setTimeout(resolve, 600));
    await frame.contentDocument.fonts.ready;
  }, { data, width: WIDTH, height: HEIGHT });
  const png = await page.screenshot({ clip: { x: 0, y: 0, width: WIDTH, height: HEIGHT } });
  // Downscale and encode in the browser: Chromium writes WebP, so no image tool is needed.
  const base64 = await page.evaluate(async ({ png, width, height }) => {
    const image = new Image();
    image.src = `data:image/png;base64,${png}`;
    await image.decode();
    const canvas = document.createElement("canvas");
    canvas.width = width / 2;
    canvas.height = height / 2;
    const context = canvas.getContext("2d");
    context.imageSmoothingQuality = "high";
    context.drawImage(image, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL("image/webp", 0.82).split(",")[1];
  }, { png: png.toString("base64"), width: WIDTH, height: HEIGHT });
  await writeFile(new URL(entry.thumbnail, OUT), Buffer.from(base64, "base64"));
  console.log(`${entry.thumbnail} geschrieben`);
}
await browser.close();
