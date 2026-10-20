package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;

// One page for the editor: settings, draft and (if any) published content.
public record ShopPageDetailDto(ShopPageDto page, JsonNode draft, JsonNode published) {
}
