import { contrast, luminance, mix } from "../../../shop/contrast.js";
import { blockPairs, effectiveColors, failing, MUTED_SHARE, pairIdentity, themePairs } from "../contrast-pairs.js";

// Contrast rule (shop-design-assistant spec): below 4.5:1 Warnung, below 3:1 Blocker.
// The theme's own pairs are reported once for the design; a block only reports pairs that its
// overrides or its background choice make different from the theme's. Failing pairs that one
// colour fixes (text and secondary text, say) form one finding.

const ratioText = (ratio) => `${ratio.toFixed(1).replace(".", ",")}:1`;

// Text colour of the same hue that makes every pair of the group readable, secondary text
// included: mixed towards black or white in 5 % steps. null when no step gets there.
function suggestText(text, pairs) {
  const ground = pairs[0].background;
  const towards = contrast("#000000", ground) >= contrast("#ffffff", ground) ? "#000000" : "#ffffff";
  for (let step = 1; step <= 20; step += 1) {
    const candidate = mix(towards, text, step / 20);
    const readable = pairs.every((pair) => {
      const foreground = pair.mutedOn ? mix(candidate, pair.mutedOn, MUTED_SHARE) : candidate;
      return contrast(foreground, pair.background) >= pair.min;
    });
    if (readable) return candidate.toUpperCase();
  }
  return null;
}

// One finding per colour role; the worst pair names severity and ratio.
function grouped(pairs, colors) {
  const byRole = new Map();
  for (const pair of pairs) byRole.set(pair.role, [...(byRole.get(pair.role) ?? []), pair]);
  return [...byRole.entries()].map(([role, group]) => {
    const worst = group.reduce((a, b) => (b.ratio < a.ratio ? b : a));
    return { key: role, role, severity: worst.severity, worst, labels: group.map((pair) => pair.label), text: colors.text, group };
  });
}

const others = (count) => (count === 0 ? "" : ` und ${count === 1 ? "eine weitere Stelle" : `${count} weitere Stellen`}`);

function explain({ worst, labels, role, text, group }) {
  const suggestion = role === "text" ? suggestText(text, group) : null;
  const fix = role === "text"
    ? `Wählen Sie eine ${suggestion && luminance(suggestion) > luminance(text) ? "hellere" : "dunklere"} Textfarbe${suggestion ? `, zum Beispiel ${suggestion}` : ""}, oder ändern Sie den Hintergrund.`
    : "Wählen Sie für diese Farbe einen deutlich helleren oder dunkleren Ton. Die Textfarbe darauf wählt der Shop selbst.";
  return {
    title: worst.severity === "blocker" ? "Text ist kaum lesbar" : "Text ist schwer lesbar",
    why: `${worst.label}${others(labels.length - 1)}: Kontrast ${ratioText(worst.ratio)}, empfohlen sind mindestens ${ratioText(worst.min)}. `
      + "Viele Menschen, vor allem am Handy in der Sonne oder mit schwächeren Augen, können den Text sonst nicht lesen.",
    fix,
  };
}

export const themeContrast = {
  id: "contrast-theme",
  severity: "warning",
  scope: "theme",
  check: ({ theme }) => grouped(failing(themePairs(theme.colors)), theme.colors),
  explain,
};

export const blockContrast = {
  id: "contrast-block",
  severity: "warning",
  scope: "block",
  check({ theme, blocks }) {
    const ofTheme = new Set(themePairs(theme.colors).map(pairIdentity));
    return blocks.flatMap((block) => {
      const colors = effectiveColors(theme, block);
      const own = failing(blockPairs(block, colors, theme.form)).filter((pair) => !ofTheme.has(pairIdentity(pair)));
      return grouped(own, colors).map((finding) => ({ ...finding, blockId: block.id }));
    });
  },
  explain,
};
