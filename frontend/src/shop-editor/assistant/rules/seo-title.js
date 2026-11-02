export const seoTitle = {
  id: "seo-title",
  severity: "tip",
  scope: "page",
  check: ({ page }) => (page.online && !String(page.seoTitle ?? "").trim() ? [{ target: "page-settings" }] : []),
  explain: () => ({
    title: "SEO-Titel fehlt",
    why: "Google zeigt den SEO-Titel als Überschrift in den Suchergebnissen. Ohne ihn steht dort nur der Menüname.",
    fix: "Tragen Sie in den Seiteneinstellungen einen kurzen, treffenden SEO-Titel ein.",
  }),
};
