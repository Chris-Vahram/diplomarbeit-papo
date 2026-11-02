// Startseite only: the first Hero/Banner is where visitors see whose shop this is.
export const logo = {
  id: "logo",
  severity: "tip",
  scope: "page",
  check({ page, blocks }) {
    if (page.slot !== "HOME") return [];
    const hero = blocks.find((block) => block.type === "hero");
    return hero?.config?.logoId ? [] : [{ blockId: hero?.id ?? null, field: "logoId", target: hero ? "block" : "palette" }];
  },
  explain: ({ blockId }) => ({
    title: "Ein Logo macht Ihren Shop wiedererkennbar",
    why: "Kundinnen und Kunden erkennen auf den ersten Blick, bei wem sie einkaufen.",
    fix: blockId
      ? "Wählen Sie im Abschnitt „Hero/Banner“ unter „Logo“ Ihr Firmenlogo aus."
      : "Fügen Sie der Startseite einen Abschnitt „Hero/Banner“ hinzu und wählen Sie dort Ihr Logo.",
  }),
};
