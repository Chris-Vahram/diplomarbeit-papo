package at.papo.partnerportal.shop;

import java.util.Arrays;
import java.util.Optional;

// Curated self-hosted fonts (shop-theme spec "Curated self-hosted fonts"). Every entry except
// SYSTEM has woff2 files under frontend/vendor/fonts and an @font-face in src/shop/shop-fonts.css;
// all are SIL OFL 1.1 (frontend/vendor/fonts/OFL.txt). Final selection open with Tobias.
public enum ShopFont {
    SYSTEM("system", "Systemschrift"),
    PLUS_JAKARTA_SANS("plus-jakarta-sans", "Plus Jakarta Sans"),
    INTER("inter", "Inter"),
    DM_SANS("dm-sans", "DM Sans"),
    WORK_SANS("work-sans", "Work Sans"),
    NUNITO("nunito", "Nunito"),
    MONTSERRAT("montserrat", "Montserrat"),
    SOURCE_SANS_3("source-sans-3", "Source Sans 3"),
    OPEN_SANS("open-sans", "Open Sans"),
    LORA("lora", "Lora"),
    MERRIWEATHER("merriweather", "Merriweather"),
    PLAYFAIR_DISPLAY("playfair-display", "Playfair Display");

    public final String id;
    public final String family;

    ShopFont(String id, String family) {
        this.id = id;
        this.family = family;
    }

    public static Optional<ShopFont> byId(String id) {
        return Arrays.stream(values()).filter(font -> font.id.equals(id)).findFirst();
    }
}
