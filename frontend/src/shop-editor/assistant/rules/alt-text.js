// Missing alt text: an emphasised Warnung, never a Blocker (ENDKUNDENSHOP_DECISIONS §4).
export const altText = {
  id: "alt-text",
  severity: "warning",
  scope: "block",
  emphasis: true,
  check({ blocks }) {
    return blocks
      .filter((block) => block.config?.imageId && !String(block.config.alt ?? "").trim())
      .map((block) => ({ blockId: block.id, field: "alt" }));
  },
  explain: () => ({
    title: "Bildbeschreibung fehlt",
    why: "Menschen, die sich Seiten vorlesen lassen, und Suchmaschinen erfahren sonst nicht, was das Bild zeigt.",
    fix: "Beschreiben Sie im Feld „Bildbeschreibung (Alt-Text)“ in einem kurzen Satz, was zu sehen ist.",
  }),
};
