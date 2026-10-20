import { resolveImageUrl } from "../lib/product-images.js";

// Data for one shop.html?preview frame (shop-app.js renderPreview). Shared by the builder's
// live preview and the page thumbnails on "Mein Webshop", so both render identically.
//
// preview: ShopPreviewDto, theme: theme draft, pages: page list (ShopPageDto[]), page: the
// page shown, blocks: its draft blocks; assetUrls/imageUrls: object URLs (see below).
export function previewPayload({ preview, theme, pages, page, blocks, assetUrls = {}, imageUrls = {}, selectedBlockId = null }) {
  // Navigation from the editor's own page list, so renamed or reordered pages show at once.
  const items = pages
    .filter((candidate) => candidate.online && candidate.slot !== "ARTICLE_DETAIL")
    .map((candidate) => ({
      segment: candidate.segment,
      menuName: candidate.menuName,
      path: candidate.slot === "HOME" ? "" : candidate.segment,
    }));
  return {
    shop: preview.shop,
    theme,
    navigation: { ...preview.navigation, items },
    page: {
      menuName: page.menuName,
      path: page.slot === "HOME" ? "" : page.slot === "ARTICLE_DETAIL" ? "artikel" : page.segment,
      blocks,
    },
    article: page.slot === "ARTICLE_DETAIL" ? preview.articles[0] ?? null : null,
    articles: preview.articles,
    assetUrls,
    imageUrls,
    selectedBlockId,
  };
}

export function postPreview(frame, payload) {
  frame.contentWindow?.postMessage({ type: "papo-shop-preview", payload }, location.origin);
}

// Article images of a not yet public shop come through the portal and need object URLs.
export async function articleImageUrls(articles) {
  const urls = [...new Set(articles.map((article) => article.imageUrl).filter((url) => url?.startsWith("/product-images/")))];
  const resolved = {};
  await Promise.all(
    urls.map(async (url) => {
      try {
        resolved[url] = await resolveImageUrl(url);
      } catch {
        // Missing image: the card shows its placeholder.
      }
    }),
  );
  return resolved;
}

export function imageIds(blocks) {
  return [...new Set(blocks.map((block) => block.config?.imageId).filter(Boolean))];
}
