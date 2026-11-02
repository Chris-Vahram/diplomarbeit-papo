// Largest width in CSS pixels an image block is shown at on a desktop screen (shop.css:
// content max. 76rem, hero composition per form). Rough by design: the rule only flags images
// that are clearly too small.
const HERO = { klar: 640, handwerk: 1216, kraeftig: 1600 };
const IMAGE = { narrow: 704, wide: 1216, full: 1600 };

export function displayWidth(block, form = "klar") {
  switch (block.type) {
    case "hero":
      return HERO[form] ?? HERO.klar;
    case "image":
      return IMAGE[block.config?.width ?? "wide"] ?? IMAGE.wide;
    case "image-text":
      return 600;
    default:
      return null;
  }
}
