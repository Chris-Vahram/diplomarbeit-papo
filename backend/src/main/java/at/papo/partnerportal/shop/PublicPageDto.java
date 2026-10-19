package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;

// Published version of a page only; drafts never leave the server through the public API.
public record PublicPageDto(String segment, String menuName, ShopPageSlot slot, JsonNode blocks) {
}
