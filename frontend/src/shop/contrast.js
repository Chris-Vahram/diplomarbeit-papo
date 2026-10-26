// WCAG 2.x contrast for #RRGGBB colours (shop-design-assistant spec "Contrast rule"). Used by
// the renderer (readable text on filled backgrounds) and the editor's design assistant.
// Mirrored in the backend by ShopContrast; both run the shared vectors in contrast-vectors.json.

function channel(hex, offset) {
  const value = parseInt(hex.slice(offset, offset + 2), 16) / 255;
  return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
}

export function luminance(hex) {
  return 0.2126 * channel(hex, 1) + 0.7152 * channel(hex, 3) + 0.0722 * channel(hex, 5);
}

// (lighter + 0.05) / (darker + 0.05), from 1 (same colour) to 21 (black on white).
export function contrast(a, b) {
  const la = luminance(a);
  const lb = luminance(b);
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

// Readable text colour on a filled background: whichever of dark and white has the higher contrast.
export function onColor(hex) {
  return contrast("#111111", hex) > contrast("#ffffff", hex) ? "#111111" : "#ffffff";
}

// color-mix(in srgb, a share, b), as shop.css derives --shop-muted.
export function mix(a, b, share) {
  let out = "#";
  for (const offset of [1, 3, 5]) {
    const x = parseInt(a.slice(offset, offset + 2), 16);
    const y = parseInt(b.slice(offset, offset + 2), 16);
    out += Math.round(x * share + y * (1 - share)).toString(16).padStart(2, "0");
  }
  return out;
}

// Nearest colour of the same hue that reaches `min` on `background`: the foreground mixed
// towards black or white, whichever side can get there. null when neither can.
export function suggestForeground(foreground, background, min = 4.5) {
  const towards = contrast("#000000", background) >= contrast("#ffffff", background) ? "#000000" : "#ffffff";
  for (let step = 1; step <= 20; step += 1) {
    const candidate = mix(towards, foreground, step / 20);
    if (contrast(candidate, background) >= min) return candidate.toUpperCase();
  }
  return null;
}
