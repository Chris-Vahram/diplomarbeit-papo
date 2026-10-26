export const emptyPage = {
  id: "empty-page",
  severity: "warning",
  scope: "page",
  check: ({ blocks }) => (blocks.length ? [] : [{ target: "palette" }]),
  explain: () => ({
    title: "Diese Seite ist leer",
    why: "Besucherinnen und Besucher sehen nur Kopf- und Fußzeile und verlassen die Seite wieder.",
    fix: "Fügen Sie links unter „Hinzufügen“ einen Abschnitt hinzu oder wählen Sie eine Vorlage.",
  }),
};
