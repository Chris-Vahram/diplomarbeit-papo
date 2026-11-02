import { displayWidth } from "../display-width.js";

// Image file size and resolution from ctx.images[assetId] = { sizeBytes, width }; both are
// filled in as the editor learns them, so a rule simply skips what is not known yet.
export const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

const IMAGE_FIELDS = ["imageId", "logoId"];

function images(blocks, ctx) {
  return blocks.flatMap((block) => IMAGE_FIELDS
    .filter((field) => block.config?.[field] != null)
    .map((field) => ({ block, field, info: ctx.images?.[block.config[field]] ?? {} })));
}

export const imageTooLarge = {
  id: "image-too-large",
  severity: "warning",
  scope: "block",
  check: (ctx) => images(ctx.blocks, ctx)
    .filter(({ info }) => info.sizeBytes > MAX_IMAGE_BYTES)
    .map(({ block, field, info }) => ({ blockId: block.id, field, key: field, sizeBytes: info.sizeBytes })),
  explain: ({ sizeBytes }) => ({
    title: "Bild lädt langsam",
    why: `Die Datei ist ${(sizeBytes / (1024 * 1024)).toFixed(1).replace(".", ",")} MB groß. Am Handy dauert das lange und kostet Datenvolumen.`,
    fix: "Speichern Sie das Bild kleiner (unter 2 MB, z. B. als JPG oder WebP) und laden Sie es neu hoch.",
  }),
};

export const imageTooSmall = {
  id: "image-too-small",
  severity: "tip",
  scope: "block",
  check: (ctx) => images(ctx.blocks, ctx)
    .filter(({ field }) => field === "imageId")
    .map((entry) => ({ ...entry, needed: displayWidth(entry.block, ctx.theme.form) }))
    .filter(({ info, needed }) => info.width && needed && info.width < needed)
    .map(({ block, field, info, needed }) => ({ blockId: block.id, field, width: info.width, needed })),
  explain: ({ width, needed }) => ({
    title: "Bild wirkt unscharf",
    why: `Das Bild ist ${width} Pixel breit, wird aber bis zu ${needed} Pixel breit angezeigt.`,
    fix: `Laden Sie eine größere Fassung hoch, mindestens ${needed} Pixel breit.`,
  }),
};
