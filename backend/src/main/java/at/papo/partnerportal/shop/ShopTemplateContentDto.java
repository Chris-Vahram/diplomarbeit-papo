package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;

// A template ready for one shop: blocks with ids and the shop's own image ids, plus the theme
// suggestion (null if none) that the editor applies only on "Farben und Schriften übernehmen".
public record ShopTemplateContentDto(JsonNode blocks, JsonNode theme) {
}
