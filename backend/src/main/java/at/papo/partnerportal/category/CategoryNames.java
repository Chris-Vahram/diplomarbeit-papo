package at.papo.partnerportal.category;

import at.papo.partnerportal.common.ApiException;

import java.util.Locale;

public final class CategoryNames {

    private CategoryNames() {
    }

    public static String requireValid(String value) {
        String name = value == null ? null : value.strip();
        if (name == null || name.isEmpty()) {
            throw new ApiException(400, "Artikelkategorie ist erforderlich.");
        }
        if (name.length() > 255) {
            throw new ApiException(400, "Artikelkategorie darf höchstens 255 Zeichen lang sein.");
        }
        return name;
    }

    public static String normalized(String value) {
        return requireValid(value).toLowerCase(Locale.ROOT);
    }
}
