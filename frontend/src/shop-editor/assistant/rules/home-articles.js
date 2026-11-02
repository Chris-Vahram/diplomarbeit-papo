const ARTICLE_BLOCKS = new Set(["article-grid", "featured-articles"]);

export const homeArticles = {
  id: "home-articles",
  severity: "tip",
  scope: "page",
  check: ({ page, blocks }) =>
    (page.slot === "HOME" && !blocks.some((block) => ARTICLE_BLOCKS.has(block.type)) ? [{ target: "palette" }] : []),
  explain: () => ({
    title: "Auf der Startseite fehlen Artikel",
    why: "Wer Ihren Shop öffnet, möchte gleich sehen, was es bei Ihnen gibt.",
    fix: "Fügen Sie ein „Artikelraster“ oder „Hervorgehobene Artikel“ hinzu.",
  }),
};
