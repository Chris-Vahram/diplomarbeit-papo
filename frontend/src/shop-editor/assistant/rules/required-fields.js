import { BLOCK_TYPES, isEmpty } from "../../block-schema.js";

// Empty required field: the page cannot be published (server: ShopBlockValidator).
export const requiredFields = {
  id: "required-field",
  severity: "blocker",
  scope: "block",
  check: ({ blocks }) => blocks.flatMap((block) => (BLOCK_TYPES[block.type]?.fields ?? [])
    .filter((field) => field.required && isEmpty(block.config?.[field.name]))
    .map((field) => ({ blockId: block.id, field: field.name, key: field.name, label: field.label }))),
  explain: ({ label }) => ({
    title: `„${label}“ fehlt`,
    why: "Ohne dieses Feld kann der Abschnitt nicht angezeigt werden, deshalb lässt sich die Seite so nicht veröffentlichen.",
    fix: `Füllen Sie „${label}“ aus oder löschen Sie den Abschnitt.`,
  }),
};
