package at.papo.partnerportal.shop;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static at.papo.partnerportal.shop.ShopBlockField.articles;
import static at.papo.partnerportal.shop.ShopBlockField.Kind.BOOLEAN;
import static at.papo.partnerportal.shop.ShopBlockField.Kind.CATEGORY;
import static at.papo.partnerportal.shop.ShopBlockField.Kind.IMAGE;
import static at.papo.partnerportal.shop.ShopBlockField.Kind.SEGMENT;
import static at.papo.partnerportal.shop.ShopBlockField.choice;
import static at.papo.partnerportal.shop.ShopBlockField.of;
import static at.papo.partnerportal.shop.ShopBlockField.required;
import static at.papo.partnerportal.shop.ShopBlockField.requiredText;
import static at.papo.partnerportal.shop.ShopBlockField.text;

// Block catalogue (shop-pages spec "Block catalogue"). A new block type = one entry here plus
// one custom element in frontend/src/shop/blocks/ (design.md Decision 1). Every type also
// accepts ShopBlockField.COMMON: spacing, background and colour overrides.
public enum ShopBlockType {
    HERO("hero", "Hero/Banner",
            of("logoId", IMAGE), requiredText("title", 120), text("text", 400), of("imageId", IMAGE), text("alt", 200),
            text("buttonLabel", 40), of("buttonPage", SEGMENT), choice("align", "left", "center")),
    HEADING_TEXT("heading-text", "Überschrift + Text",
            text("heading", 120), text("text", 4000), choice("align", "left", "center")),
    IMAGE_BLOCK("image", "Bild",
            required("imageId", IMAGE), text("alt", 200), text("caption", 200),
            choice("width", "narrow", "wide", "full")),
    IMAGE_TEXT("image-text", "Bild + Text",
            required("imageId", IMAGE), text("alt", 200), text("heading", 120), text("text", 2000),
            choice("imagePosition", "left", "right")),
    ARTICLE_GRID("article-grid", "Artikelraster",
            text("heading", 120), of("categoryId", CATEGORY), choice("columns", "2", "3", "4")),
    FEATURED_ARTICLES("featured-articles", "Hervorgehobene Artikel",
            text("heading", 120), articles("articleIds", 12), choice("columns", "2", "3", "4")),
    ARTICLE_DETAIL("article-detail", "Artikeldetail",
            choice("imagePosition", "left", "right")),
    CONTACT_PERSON("contact-person", "Ansprechpartner",
            text("heading", 120), requiredText("name", 120), text("role", 120), text("text", 1000),
            text("email", 254), text("phone", 40)),
    SPACER("spacer", "Abstand/Trennlinie",
            choice("size", "small", "medium", "large"), of("divider", BOOLEAN));

    public final String id;
    public final String label;
    final Map<String, ShopBlockField> fields;

    ShopBlockType(String id, String label, ShopBlockField... ownFields) {
        this.id = id;
        this.label = label;
        Map<String, ShopBlockField> all = new LinkedHashMap<>();
        Stream.concat(Arrays.stream(ownFields), ShopBlockField.COMMON.stream()).forEach(field -> all.put(field.name(), field));
        this.fields = Map.copyOf(all);
    }

    public static Optional<ShopBlockType> byId(String id) {
        return Arrays.stream(values()).filter(type -> type.id.equals(id)).findFirst();
    }
}
