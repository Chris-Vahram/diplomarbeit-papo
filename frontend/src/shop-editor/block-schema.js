// Client-side form description per block type (design.md Decision 4). Mirrors the backend
// ShopBlockType/ShopBlockField registry; the server validation stays authoritative.
//
// Field kinds: text, choice, boolean, image (shop asset), category, articles, segment (linked page).

const ALIGN = { left: "Links", center: "Zentriert" };
const COLUMNS = { 2: "2 Spalten", 3: "3 Spalten", 4: "4 Spalten" };
const IMAGE_SIDE = { left: "Bild links", right: "Bild rechts" };

const text = (name, label, maxLength, extra = {}) => ({ name, label, kind: "text", maxLength, ...extra });
const choice = (name, label, choices) => ({ name, label, kind: "choice", choices });

export const BLOCK_TYPES = {
  hero: {
    label: "Hero/Banner",
    description: "Großer Einstieg mit Titel, Text und Schaltfläche",
    fields: [
      text("title", "Titel", 120, { required: true }),
      text("text", "Text", 400, { multiline: true }),
      { name: "imageId", label: "Hintergrundbild", kind: "image" },
      text("alt", "Bildbeschreibung (Alt-Text)", 200, { hint: "Beschreibt das Bild für blinde Menschen und Suchmaschinen." }),
      text("buttonLabel", "Beschriftung der Schaltfläche", 40),
      { name: "buttonPage", label: "Schaltfläche führt zu", kind: "segment" },
      choice("align", "Ausrichtung", ALIGN),
    ],
    defaults: { title: "Neuer Titel", align: "center", background: "primary" },
  },
  "heading-text": {
    label: "Überschrift + Text",
    description: "Überschrift mit Fließtext",
    fields: [
      text("heading", "Überschrift", 120),
      text("text", "Text", 4000, { multiline: true }),
      choice("align", "Ausrichtung", ALIGN),
    ],
    defaults: { heading: "Neue Überschrift", text: "" },
  },
  image: {
    label: "Bild",
    description: "Ein einzelnes Bild mit Bildunterschrift",
    fields: [
      { name: "imageId", label: "Bild", kind: "image", required: true },
      text("alt", "Bildbeschreibung (Alt-Text)", 200, { hint: "Beschreibt das Bild für blinde Menschen und Suchmaschinen." }),
      text("caption", "Bildunterschrift", 200),
      choice("width", "Breite", { narrow: "Schmal", wide: "Breit", full: "Volle Breite" }),
    ],
    defaults: { width: "wide" },
  },
  "image-text": {
    label: "Bild + Text",
    description: "Bild neben Überschrift und Text",
    fields: [
      { name: "imageId", label: "Bild", kind: "image", required: true },
      text("alt", "Bildbeschreibung (Alt-Text)", 200, { hint: "Beschreibt das Bild für blinde Menschen und Suchmaschinen." }),
      text("heading", "Überschrift", 120),
      text("text", "Text", 2000, { multiline: true }),
      choice("imagePosition", "Bildposition", IMAGE_SIDE),
    ],
    defaults: { heading: "Neue Überschrift", imagePosition: "left" },
  },
  "article-grid": {
    label: "Artikelraster",
    description: "Alle Artikel oder die einer Kategorie",
    fields: [
      text("heading", "Überschrift", 120),
      { name: "categoryId", label: "Kategorie", kind: "category" },
      choice("columns", "Spalten", COLUMNS),
    ],
    defaults: { heading: "Unsere Artikel", columns: "3" },
  },
  "featured-articles": {
    label: "Hervorgehobene Artikel",
    description: "Bis zu 12 ausgewählte Artikel",
    fields: [
      text("heading", "Überschrift", 120),
      { name: "articleIds", label: "Artikel", kind: "articles", maxLength: 12 },
      choice("columns", "Spalten", COLUMNS),
    ],
    defaults: { heading: "Empfehlungen", articleIds: [], columns: "3" },
  },
  "article-detail": {
    label: "Artikeldetail",
    description: "Name, Bild, Preis und Beschreibung des Artikels",
    fields: [choice("imagePosition", "Bildposition", IMAGE_SIDE)],
    defaults: { imagePosition: "left" },
    // Only on the Artikeldetail page, exactly once (shop-pages spec).
    fixed: true,
  },
  "contact-person": {
    label: "Ansprechpartner",
    description: "Name, Funktion und Kontaktdaten",
    fields: [
      text("heading", "Überschrift", 120),
      text("name", "Name", 120, { required: true }),
      text("role", "Funktion", 120),
      text("text", "Text", 1000, { multiline: true }),
      text("email", "E-Mail", 254, { type: "email" }),
      text("phone", "Telefon", 40, { type: "tel" }),
    ],
    defaults: { heading: "Ihr Ansprechpartner", name: "" },
  },
  spacer: {
    label: "Abstand/Trennlinie",
    description: "Luft zwischen Abschnitten, optional mit Linie",
    fields: [
      choice("size", "Größe", { small: "Klein", medium: "Mittel", large: "Groß" }),
      { name: "divider", label: "Trennlinie anzeigen", kind: "boolean" },
    ],
    defaults: { size: "medium", divider: false },
  },
};

// Layout options every block accepts (backend ShopBlockField.COMMON).
export const COMMON_FIELDS = [
  choice("spacing", "Abstand", { small: "Klein", medium: "Mittel", large: "Groß" }),
  choice("background", "Hintergrund", { none: "Kein", surface: "Fläche", primary: "Hauptfarbe", accent: "Akzentfarbe" }),
];

export const COLOR_ROLES = [
  { role: "primary", label: "Hauptfarbe" },
  { role: "accent", label: "Akzentfarbe" },
  { role: "background", label: "Seitenhintergrund" },
  { role: "surface", label: "Flächen" },
  { role: "text", label: "Text" },
];

export function newBlock(type) {
  return { id: crypto.randomUUID(), type, config: structuredClone(BLOCK_TYPES[type].defaults ?? {}) };
}

function isEmpty(value) {
  return value === undefined || value === null || (typeof value === "string" && !value.trim())
    || (Array.isArray(value) && value.length === 0);
}

// Required fields in plain German before publishing, so the partner never sees the
// technical field names of the server message.
export function missingRequired(blocks) {
  const problems = [];
  blocks.forEach((block, index) => {
    const type = BLOCK_TYPES[block.type];
    for (const field of type?.fields ?? []) {
      if (field.required && isEmpty(block.config?.[field.name])) {
        problems.push({ blockId: block.id, message: `Abschnitt ${index + 1} („${type.label}“): ${field.label} fehlt.` });
      }
    }
  });
  return problems;
}

// "Über uns" -> "ueber-uns" (shop-builder spec "URL segment suggestion").
export function suggestSegment(title) {
  return String(title ?? "")
    .toLowerCase()
    .replace(/ä/g, "ae").replace(/ö/g, "oe").replace(/ü/g, "ue").replace(/ß/g, "ss")
    .normalize("NFD").replace(/[̀-ͯ]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 60)
    .replace(/-+$/g, "");
}
