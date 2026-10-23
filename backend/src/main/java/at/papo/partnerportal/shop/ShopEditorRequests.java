package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

// Request bodies of the shop editor. Every write that can overwrite someone else's work
// carries the version the editor loaded (shop-builder spec "Concurrent editing protection").
public final class ShopEditorRequests {

    private ShopEditorRequests() {
    }

    static <T> T require(T body) {
        if (body == null) {
            throw new ApiException(400, "Die Anfrage enthält keine Daten.");
        }
        return body;
    }

    // template: "leer" (default) or the id of a free-page catalogue template.
    public record CreatePage(String menuName, String segment, String template) {
    }

    public record PageSettings(String menuName, String segment, String seoTitle, String seoDescription, Long version) {
    }

    public record Draft(JsonNode content, Long version) {
    }

    public record Version(Long version) {
    }

    public record PageOrder(List<Long> pageIds) {
    }

    public record ThemeDraft(JsonNode theme, Long version) {
    }
}
