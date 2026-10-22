package at.papo.partnerportal.shop;

import java.util.List;
import java.util.Set;

// One allowed field of a block type's configuration schema.
record ShopBlockField(String name, Kind kind, int maxLength, boolean required, Set<String> choices) {

    // Layout options every block type accepts (enum constants cannot see their own statics).
    static final List<ShopBlockField> COMMON = List.of(
            choice("spacing", "small", "medium", "large"),
            choice("background", "none", "surface", "primary", "accent"),
            of("colors", Kind.COLORS));

    enum Kind {
        // Plain text, escaped on rendering; maxLength = characters.
        TEXT,
        // One of choices.
        CHOICE,
        BOOLEAN,
        // Id of one of the shop's own ShopAssets.
        IMAGE,
        // Category id that must exist.
        CATEGORY,
        // List of article ids enabled for the shop's partner; maxLength = max. entries.
        ARTICLES,
        // URL segment of a linkable page of the same shop (link target).
        SEGMENT,
        // Partial colour override, see ShopColors.
        COLORS
    }

    static ShopBlockField text(String name, int maxLength) {
        return new ShopBlockField(name, Kind.TEXT, maxLength, false, Set.of());
    }

    static ShopBlockField requiredText(String name, int maxLength) {
        return new ShopBlockField(name, Kind.TEXT, maxLength, true, Set.of());
    }

    static ShopBlockField choice(String name, String... choices) {
        return new ShopBlockField(name, Kind.CHOICE, 0, false, Set.of(choices));
    }

    static ShopBlockField of(String name, Kind kind) {
        return new ShopBlockField(name, kind, 0, false, Set.of());
    }

    static ShopBlockField required(String name, Kind kind) {
        return new ShopBlockField(name, kind, 0, true, Set.of());
    }

    static ShopBlockField articles(String name, int maxEntries) {
        return new ShopBlockField(name, Kind.ARTICLES, maxEntries, false, Set.of());
    }
}
