package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

// Public shop identifier (partner-shop spec "Shop identifier"): 3-40 chars, a-z, 0-9 and
// single inner hyphens. Suggested from the partner name, never from the partner number.
public final class ShopKennung {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 40;

    private static final Pattern VALID = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    // Path segments the platform uses or may use itself.
    static final Set<String> RESERVED = Set.of(
            "admin", "api", "assets", "auth", "company", "help", "hilfe", "impressum",
            "index", "login", "logout", "papo", "partner", "portal", "shop", "src",
            "static", "vendor", "www");

    private ShopKennung() {
    }

    public static boolean isValid(String kennung) {
        return kennung != null
                && kennung.length() >= MIN_LENGTH
                && kennung.length() <= MAX_LENGTH
                && VALID.matcher(kennung).matches()
                && !RESERVED.contains(kennung);
    }

    // Validates company input; trims surrounding whitespace only, so "Huemer Shop!" is rejected.
    public static String requireValid(String input) {
        String kennung = input == null ? "" : input.strip();
        if (kennung.length() < MIN_LENGTH || kennung.length() > MAX_LENGTH) {
            throw new ApiException(400, "Die Kennung muss zwischen 3 und 40 Zeichen lang sein.");
        }
        if (!VALID.matcher(kennung).matches()) {
            throw new ApiException(400,
                    "Die Kennung darf nur Kleinbuchstaben a–z, Ziffern und einzelne Bindestriche enthalten "
                            + "und nicht mit einem Bindestrich beginnen oder enden.");
        }
        if (RESERVED.contains(kennung)) {
            throw new ApiException(400, "Diese Kennung ist reserviert. Bitte eine andere wählen.");
        }
        return kennung;
    }

    // "Huemer Elektro GmbH" -> "huemer-elektro-gmbh"; umlauts become ae/oe/ue, ß becomes ss.
    public static String slugify(String name) {
        String value = name == null ? "" : name.toLowerCase(Locale.ROOT)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        value = value.replaceAll("[^a-z0-9]+", "-");
        return trimHyphens(truncate(trimHyphens(value), MAX_LENGTH));
    }

    // Suggestion that is valid and not taken; appends -2, -3, ... on collision.
    public static String suggest(String partnerName, Predicate<String> taken) {
        String base = slugify(partnerName);
        if (!isValid(base)) {
            base = base.isEmpty() ? "partner-shop" : trimHyphens(truncate("shop-" + base, MAX_LENGTH));
        }
        if (!taken.test(base)) {
            return base;
        }
        for (int number = 2; ; number++) {
            String suffix = "-" + number;
            String candidate = trimHyphens(truncate(base, MAX_LENGTH - suffix.length())) + suffix;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
    }

    private static String truncate(String value, int length) {
        return value.length() <= length ? value : value.substring(0, length);
    }

    private static String trimHyphens(String value) {
        return value.replaceAll("^-+|-+$", "");
    }
}
