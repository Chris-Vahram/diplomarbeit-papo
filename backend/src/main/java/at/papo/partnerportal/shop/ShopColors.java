package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// Theme colour roles and #RRGGBB validation, shared by the theme and block overrides.
// The strict format is what keeps colour values safe to write into CSS custom properties.
final class ShopColors {

    static final List<String> ROLES = List.of("primary", "accent", "background", "surface", "text");

    private static final Pattern HEX = Pattern.compile("#[0-9A-Fa-f]{6}");

    private ShopColors() {
    }

    // Theme: all roles required. Block override: any subset.
    static void validate(JsonNode colors, boolean allRequired, String where) {
        if (colors == null || !colors.isObject()) {
            throw new ApiException(400, "Farben fehlen oder sind ungültig" + where + ".");
        }
        for (Iterator<Map.Entry<String, JsonNode>> it = colors.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            if (!ROLES.contains(entry.getKey())) {
                throw new ApiException(400, "Unbekannte Farbe „" + entry.getKey() + "“" + where + ".");
            }
            if (!entry.getValue().isTextual() || !HEX.matcher(entry.getValue().asText()).matches()) {
                throw new ApiException(400, "Farbe „" + entry.getKey() + "“ muss im Format #RRGGBB angegeben werden.");
            }
        }
        if (allRequired) {
            for (String role : ROLES) {
                if (!colors.has(role)) {
                    throw new ApiException(400, "Farbe „" + role + "“ fehlt" + where + ".");
                }
            }
        }
    }
}
