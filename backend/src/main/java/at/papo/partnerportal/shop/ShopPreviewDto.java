package at.papo.partnerportal.shop;

import java.util.List;

// Everything the editor preview renders besides the draft itself: the same shapes as the
// public API, available before the shop is released.
public record ShopPreviewDto(PublicShopDto shop, PublicNavigationDto navigation, List<PublicArticleDto> articles,
                             List<PublicCategoryDto> categories) {
}
