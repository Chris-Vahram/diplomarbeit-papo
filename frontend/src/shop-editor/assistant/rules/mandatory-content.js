// Mandatory page content (server: ShopBlockValidator): the Artikeldetail page shows the article
// through exactly one "Artikeldetail" block, and no other page may contain one.
export const mandatoryContent = {
  id: "mandatory-content",
  severity: "blocker",
  scope: "page",
  check({ page, blocks }) {
    const details = blocks.filter((block) => block.type === "article-detail");
    if (page.slot === "ARTICLE_DETAIL") return details.length === 1 ? [] : [{ count: details.length }];
    return details.map((block) => ({ blockId: block.id, misplaced: true }));
  },
  explain: ({ misplaced, count }) => ({
    title: misplaced ? "„Artikeldetail“ gehört auf die Seite Artikeldetail" : "Der Artikel selbst fehlt",
    why: misplaced
      ? "Dieser Abschnitt zeigt einen einzelnen Artikel und funktioniert nur auf der Seite Artikeldetail."
      : `Die Seite Artikeldetail braucht genau einen Abschnitt „Artikeldetail“, hier sind es ${count}.`,
    fix: misplaced ? "Löschen Sie den Abschnitt auf dieser Seite." : "Wählen Sie die Vorlage für Artikeldetail neu oder stellen Sie die vorherige Version wieder her.",
  }),
};
