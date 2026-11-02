// Rule registry of the design assistant (shop-design-assistant spec "Extensible rule set").
// A rule is { id, severity, scope: "page" | "block" | "theme", check(ctx), explain(finding) },
// optionally emphasis: true. A new rule = one module here plus its entry in this list.
import { altText } from "./alt-text.js";
import { blockContrast, themeContrast } from "./contrast.js";
import { emptyPage } from "./empty-page.js";
import { homeArticles } from "./home-articles.js";
import { imageTooLarge, imageTooSmall } from "./image-size.js";
import { logo } from "./logo.js";
import { mandatoryContent } from "./mandatory-content.js";
import { requiredFields } from "./required-fields.js";
import { seoTitle } from "./seo-title.js";

export const RULES = [
  requiredFields,
  mandatoryContent,
  themeContrast,
  blockContrast,
  altText,
  emptyPage,
  imageTooLarge,
  logo,
  homeArticles,
  imageTooSmall,
  seoTitle,
];
