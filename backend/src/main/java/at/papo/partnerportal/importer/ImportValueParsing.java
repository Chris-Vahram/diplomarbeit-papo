package at.papo.partnerportal.importer;

import java.math.BigDecimal;
import java.util.Set;

// Small shared parsing helpers for import row values - file-sourced strings
// (German-locale spreadsheet exports) need more lenient parsing than API JSON.
final class ImportValueParsing {

    private static final Set<String> TRUE_VALUES = Set.of("ja", "true", "1", "yes", "wahr");
    private static final Set<String> FALSE_VALUES = Set.of("nein", "false", "0", "no", "falsch");

    private ImportValueParsing() {
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    // Accepts both "12.50" and the German "12,50".
    static BigDecimal parseDecimal(String value) {
        return new BigDecimal(value.trim().replace(",", "."));
    }

    // null = present but unparseable; caller treats blank separately.
    static Boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        if (TRUE_VALUES.contains(normalized)) {
            return true;
        }
        if (FALSE_VALUES.contains(normalized)) {
            return false;
        }
        return null;
    }
}
