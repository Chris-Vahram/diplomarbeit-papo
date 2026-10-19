package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopKennungTest {

    @Test
    void suggestsFromPartnerName() {
        assertEquals("huemer-elektro-gmbh", ShopKennung.suggest("Huemer Elektro GmbH", k -> false));
    }

    @Test
    void transliteratesUmlautsAndAccents() {
        assertEquals("mueller-soehne-strasse", ShopKennung.slugify("Müller & Söhne  Straße"));
        assertEquals("cafe-weiss", ShopKennung.slugify("Café Weiß"));
    }

    @Test
    void appendsSuffixOnCollision() {
        Set<String> taken = Set.of("huemer-elektro-gmbh", "huemer-elektro-gmbh-2");
        assertEquals("huemer-elektro-gmbh-3", ShopKennung.suggest("Huemer Elektro GmbH", taken::contains));
    }

    @Test
    void keepsSuggestionWithinMaxLengthIncludingSuffix() {
        String longName = "Sehr Lange Firmenbezeichnung Für Elektrotechnik Und Installationen";
        String first = ShopKennung.suggest(longName, k -> false);
        String second = ShopKennung.suggest(longName, first::equals);
        assertTrue(first.length() <= ShopKennung.MAX_LENGTH);
        assertTrue(second.length() <= ShopKennung.MAX_LENGTH);
        assertTrue(second.endsWith("-2"));
        assertTrue(ShopKennung.isValid(second));
    }

    @Test
    void replacesReservedOrTooShortSuggestion() {
        assertEquals("shop-admin", ShopKennung.suggest("Admin", k -> false));
        assertEquals("shop-ab", ShopKennung.suggest("AB", k -> false));
        assertEquals("partner-shop", ShopKennung.suggest("!!!", k -> false));
    }

    @Test
    void rejectsInvalidCompanyInput() {
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("Huemer Shop!"));
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("api"));
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("-huemer"));
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("huemer--shop"));
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("ab"));
        assertThrows(ApiException.class, () -> ShopKennung.requireValid("a".repeat(41)));
    }

    @Test
    void acceptsValidInputAndTrims() {
        assertEquals("huemer-shop-2", ShopKennung.requireValid("  huemer-shop-2 "));
        assertFalse(ShopKennung.isValid("shop"));
    }
}
