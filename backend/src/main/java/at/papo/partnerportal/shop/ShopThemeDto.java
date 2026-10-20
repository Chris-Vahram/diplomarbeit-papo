package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

// Theme for the editor's "Design" tab, with the curated font list to choose from.
public record ShopThemeDto(JsonNode draft, JsonNode published, boolean draftChanged, long version, List<Font> fonts) {

    public record Font(String id, String family) {
    }
}
