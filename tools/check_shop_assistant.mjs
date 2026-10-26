// Self-check for the shop design assistant: `node tools/check_shop_assistant.mjs`
// Runs the shared contrast vectors (also used by the backend's ShopContrastTest) and the
// scenarios of the shop-design-assistant spec against the client-side rules.
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { contrast, mix, onColor, suggestForeground } from "../frontend/src/shop/contrast.js";
import { severityOf } from "../frontend/src/shop-editor/assistant/contrast-pairs.js";
import { evaluate, gate } from "../frontend/src/shop-editor/assistant/assistant.js";
import { RULES } from "../frontend/src/shop-editor/assistant/rules/index.js";

const vectors = JSON.parse(readFileSync(new URL("./fixtures/shop-contrast-vectors.json", import.meta.url)));
for (const { foreground, background, ratio, severity } of vectors.contrast) {
  const actual = contrast(foreground, background);
  assert.ok(Math.abs(actual - ratio) < 0.01, `${foreground} auf ${background}: ${actual}`);
  assert.equal(severityOf(actual), severity, `${foreground} auf ${background}`);
}
for (const { background, text } of vectors.onColor) assert.equal(onColor(background), text);
for (const { a, b, share, result } of vectors.mix) assert.equal(mix(a, b, share), result);
assert.ok(contrast(suggestForeground("#BBBBBB", "#FFFFFF"), "#FFFFFF") >= 4.5);

const theme = {
  colors: { primary: "#1B3A57", accent: "#E07A2F", background: "#F4F5F7", surface: "#FFFFFF", text: "#15191E" },
  headingFont: "system", bodyFont: "system", form: "klar",
};
const page = { slot: "FREE", online: true, seoTitle: "Über uns" };
const block = (type, config, id = `${type}-1`) => ({ id, type, config });
const run = (blocks, extra = {}) => evaluate({ page, blocks, theme, images: {}, ...extra });
const ids = (findings) => findings.map((finding) => `${finding.ruleId}/${finding.severity}`);

// The default theme itself is clean.
assert.deepEqual(ids(run([block("heading-text", { heading: "Hallo" })])), []);

// Spec "Light grey on white": Blocker "Text ist kaum lesbar" with a darker suggestion.
const grey = run([block("heading-text", { heading: "Hallo", background: "surface", colors: { text: "#BBBBBB" } })]);
const blocker = grey.find((finding) => finding.ruleId === "contrast-block");
assert.equal(blocker.severity, "blocker");
assert.equal(blocker.title, "Text ist kaum lesbar");
assert.match(blocker.fix, /dunklere Textfarbe, zum Beispiel #[0-9A-F]{6}/);
assert.equal(blocker.blockId, "heading-text-1");

// Text and secondary text are one finding; the suggestion makes both readable.
assert.equal(grey.filter((finding) => finding.ruleId === "contrast-block").length, 1);
const suggested = blocker.fix.match(/#[0-9A-F]{6}/)[0];
assert.deepEqual(ids(run([block("heading-text", { heading: "Hallo", background: "surface", colors: { text: suggested } })])), []);

// Spec "Borderline contrast": a pair at 3.5:1 is a Warnung (vector #888888 on #FFFFFF), and
// a block whose worst pair is between 3:1 and 4.5:1 gets a Warnung.
assert.equal(severityOf(contrast("#888888", "#FFFFFF")), "warning");
const border = run([block("heading-text", { heading: "Hallo", background: "surface", colors: { text: "#666666" } })]);
assert.equal(border.find((finding) => finding.ruleId === "contrast-block").severity, "warning");

// A bad theme colour is reported once for the design, not again for every block.
const badTheme = { ...theme, colors: { ...theme.colors, text: "#BBBBBB" } };
const themeFindings = evaluate({ page, blocks: [block("heading-text", {}), block("spacer", {}, "s")], theme: badTheme, images: {} });
assert.ok(themeFindings.some((finding) => finding.scope === "theme" && finding.severity === "blocker" && finding.role === "text"));
assert.ok(!themeFindings.some((finding) => finding.ruleId === "contrast-block"));

// Spec "Image without description": emphasised Warnung, listed first among warnings.
const image = run([block("image", { imageId: 7 }), block("contact-person", { name: "" })]);
assert.deepEqual(ids(image), ["required-field/blocker", "alt-text/warning"]);
assert.equal(image[1].title, "Bildbeschreibung fehlt");
assert.equal(image[1].emphasis, true);

// Further guidance rules.
assert.deepEqual(ids(run([])), ["empty-page/warning"]);
const home = evaluate({ page: { slot: "HOME", online: true, seoTitle: "" }, blocks: [block("hero", { title: "Hi" })], theme, images: {} });
assert.deepEqual(ids(home), ["logo/tip", "home-articles/tip", "seo-title/tip"]);
assert.equal(home[0].title, "Ein Logo macht Ihren Shop wiedererkennbar");
assert.equal(home[0].blockId, "hero-1");
const sized = run([block("image", { imageId: 7, alt: "Werkstatt", width: "full" })],
  { images: { 7: { sizeBytes: 3 * 1024 * 1024, width: 800 } } });
assert.deepEqual(ids(sized), ["image-too-large/warning", "image-too-small/tip"]);
assert.deepEqual(ids(evaluate({ page: { slot: "ARTICLE_DETAIL", online: true, seoTitle: "x" }, blocks: [], theme, images: {} })),
  ["mandatory-content/blocker", "empty-page/warning"]);

// Publish gating: blockers stop, warnings need confirmation, Tipps do not count.
const gated = gate(run([block("image", { imageId: 7 }), block("hero", { title: "" })]), ["page", "block"]);
assert.equal(gated.blockers.length, 1);
assert.equal(gated.warnings.length, 1);

// Spec "Adding a rule": a new unit appears without touching the others.
const extra = { id: "demo", severity: "tip", scope: "page", check: () => [{}], explain: () => ({ title: "t", why: "w", fix: "f" }) };
assert.deepEqual(ids(evaluate({ page, blocks: [block("heading-text", {})], theme, images: {} }, [...RULES, extra])), ["demo/tip"]);

console.log("shop design assistant ok");
