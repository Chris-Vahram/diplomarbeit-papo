package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Set;

// Validates a theme snapshot {"colors": {...all roles...}, "headingFont": id, "bodyFont": id}.
public final class ShopThemeValidator {

    private static final Set<String> FIELDS = Set.of("colors", "headingFont", "bodyFont");

    private ShopThemeValidator() {
    }

    public static void validate(JsonNode theme) {
        if (theme == null || !theme.isObject()) {
            throw new ApiException(400, "Das Theme ist ungültig.");
        }
        for (Iterator<String> it = theme.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            if (!FIELDS.contains(field)) {
                throw new ApiException(400, "Unbekanntes Feld „" + field + "“ im Theme.");
            }
        }
        ShopColors.validate(theme.get("colors"), true, " im Theme");
        requireFont(theme.get("headingFont"), "headingFont");
        requireFont(theme.get("bodyFont"), "bodyFont");
    }

    private static void requireFont(JsonNode value, String field) {
        if (value == null || !value.isTextual() || ShopFont.byId(value.asText()).isEmpty()) {
            throw new ApiException(400, "Die Schrift im Feld „" + field + "“ ist nicht in der Schriftenliste.");
        }
    }
}
