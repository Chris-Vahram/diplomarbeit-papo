import { luminance, suggestForeground } from "../../../shop/contrast.js";
import { blockPairs, effectiveColors, failing, pairIdentity, themePairs } from "../contrast-pairs.js";

// Contrast rule (shop-design-assistant spec): below 4.5:1 Warnung, below 3:1 Blocker.
// The theme's own pairs are reported once for the design; a block only reports pairs that its
// overrides or its background choice make different from the theme's.

const ratioText = (ratio) => `${ratio.toFixed(1).replace(".", ",")}:1`;

function explain({ pair }) {
  const suggestion = suggestForeground(pair.foreground, pair.background, pair.min);
  const automatic = pair.role === "primary" || pair.role === "accent";
  const fix = automatic
    ? "Wählen Sie für diese Farbe einen deutlich helleren oder dunkleren Ton. Die Textfarbe darauf wählt der Shop selbst."
    : `Wählen Sie eine ${suggestion && luminance(suggestion) > luminance(pair.foreground) ? "hellere" : "dunklere"} Textfarbe${suggestion ? `, zum Beispiel ${suggestion}` : ""}, oder ändern Sie den Hintergrund.`;
  return {
    title: pair.severity === "blocker" ? "Text ist kaum lesbar" : "Text ist schwer lesbar",
    why: `${pair.label}: Kontrast ${ratioText(pair.ratio)}, empfohlen sind mindestens ${ratioText(pair.min)}. `
      + "Viele Menschen, vor allem am Handy in der Sonne oder mit schwächeren Augen, können den Text sonst nicht lesen.",
    fix,
  };
}

export const themeContrast = {
  id: "contrast-theme",
  severity: "warning",
  scope: "theme",
  check({ theme }) {
    return failing(themePairs(theme.colors)).map((pair) => ({ key: pair.key, severity: pair.severity, role: pair.role, pair }));
  },
  explain,
};

export const blockContrast = {
  id: "contrast-block",
  severity: "warning",
  scope: "block",
  check({ theme, blocks }) {
    const ofTheme = new Set(themePairs(theme.colors).map(pairIdentity));
    return blocks.flatMap((block) => failing(blockPairs(block, effectiveColors(theme, block), theme.form))
      .filter((pair) => !ofTheme.has(pairIdentity(pair)))
      .map((pair) => ({ key: pair.key, blockId: block.id, severity: pair.severity, pair })));
  },
  explain,
};
