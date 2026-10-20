package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

// Reads typed field values out of a page snapshot {"blocks": [...]}, e.g. all image ids or all
// linked page segments. Unknown block types and missing fields are skipped.
final class ShopBlockContent {

    private ShopBlockContent() {
    }

    static List<JsonNode> values(JsonNode content, ShopBlockField.Kind kind) {
        List<JsonNode> values = new ArrayList<>();
        for (JsonNode block : content.path("blocks")) {
            ShopBlockType.byId(block.path("type").asText()).ifPresent(type -> type.fields.values().stream()
                    .filter(field -> field.kind() == kind)
                    .map(field -> block.path("config").path(field.name()))
                    .filter(value -> !value.isMissingNode() && !value.isNull())
                    .forEach(values::add));
        }
        return values;
    }
}
