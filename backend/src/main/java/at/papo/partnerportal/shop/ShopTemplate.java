package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

// One entry of the template catalogue (shop-templates spec). page: "startseite", "artikel",
// "artikeldetail" or "frei"; thumbnail: file under frontend/assets/shop-templates/;
// theme: optional suggestion in the theme format, null if the template has none.
public record ShopTemplate(String id, String page, String name, String description, String thumbnail,
                           JsonNode blocks, JsonNode theme) {

    // Page draft {"blocks": [...]} without block ids; ShopPageService.withBlockIds adds them.
    ObjectNode content() {
        ObjectNode content = JsonNodeFactory.instance.objectNode();
        content.set("blocks", blocks.deepCopy());
        return content;
    }
}
