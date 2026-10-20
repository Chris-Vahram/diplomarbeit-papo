package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Server-side schema check for a page snapshot {"blocks": [{"id", "type", "config"}, ...]}
// (shop-pages spec "Typed blocks with validated configuration"). Only whitelisted fields of
// known types pass; text stays plain text and is escaped by the renderer.
// Drafts are checked leniently: autosave must keep half-filled blocks and must not fail because
// an article, category or linked page changed elsewhere. Publishing checks everything.
public final class ShopBlockValidator {

    public static final int MAX_BLOCKS = 40;

    private static final Set<String> PAGE_FIELDS = Set.of("blocks");
    private static final Set<String> BLOCK_FIELDS = Set.of("id", "type", "config");

    private ShopBlockValidator() {
    }

    public static void validatePage(JsonNode content, ShopPageSlot slot, ShopBlockReferences references) {
        validatePage(content, slot, references, true);
    }

    public static void validatePage(JsonNode content, ShopPageSlot slot, ShopBlockReferences references,
                                    boolean publishing) {
        if (content == null || !content.isObject() || !content.path("blocks").isArray()) {
            throw new ApiException(400, "Der Seiteninhalt ist ungültig.");
        }
        rejectUnknown(content, PAGE_FIELDS, "im Seiteninhalt");
        JsonNode blocks = content.get("blocks");
        if (blocks.size() > MAX_BLOCKS) {
            throw new ApiException(400, "Eine Seite darf höchstens " + MAX_BLOCKS + " Blöcke enthalten.");
        }
        Set<String> ids = new HashSet<>();
        int articleDetailBlocks = 0;
        for (JsonNode block : blocks) {
            ShopBlockType type = validateBlock(block, references, publishing);
            if (!ids.add(block.get("id").asText())) {
                throw new ApiException(400, "Block-IDs müssen innerhalb einer Seite eindeutig sein.");
            }
            if (type == ShopBlockType.ARTICLE_DETAIL) {
                articleDetailBlocks++;
            }
        }
        boolean detailPage = slot == ShopPageSlot.ARTICLE_DETAIL;
        if (!detailPage && articleDetailBlocks > 0) {
            throw new ApiException(400, "Der Block „Artikeldetail“ ist nur auf der Seite Artikeldetail erlaubt.");
        }
        if (detailPage && articleDetailBlocks != 1) {
            throw new ApiException(400, "Die Seite Artikeldetail braucht genau einen Block „Artikeldetail“.");
        }
    }

    static ShopBlockType validateBlock(JsonNode block, ShopBlockReferences references, boolean publishing) {
        if (block == null || !block.isObject()) {
            throw new ApiException(400, "Ein Block ist ungültig.");
        }
        rejectUnknown(block, BLOCK_FIELDS, "in einem Block");
        if (!isUuid(block.get("id"))) {
            throw new ApiException(400, "Jeder Block braucht eine gültige ID.");
        }
        JsonNode typeNode = block.get("type");
        ShopBlockType type = typeNode != null && typeNode.isTextual()
                ? ShopBlockType.byId(typeNode.asText()).orElse(null) : null;
        if (type == null) {
            throw new ApiException(400, "Unbekannter Blocktyp „" + (typeNode == null ? "" : typeNode.asText()) + "“.");
        }
        JsonNode config = block.get("config");
        if (config == null || !config.isObject()) {
            throw new ApiException(400, "Die Konfiguration des Blocks „" + type.label + "“ fehlt.");
        }
        for (Iterator<Map.Entry<String, JsonNode>> it = config.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            ShopBlockField field = type.fields.get(entry.getKey());
            if (field == null) {
                throw new ApiException(400, "Unbekanntes Feld „" + entry.getKey() + "“ im Block „" + type.label + "“.");
            }
            if (!entry.getValue().isNull()) {
                validateField(type, field, entry.getValue(), references, publishing);
            }
        }
        for (ShopBlockField field : type.fields.values()) {
            if (publishing && field.required() && isEmpty(config.get(field.name()))) {
                throw new ApiException(400, "Das Feld „" + field.name() + "“ im Block „" + type.label + "“ ist erforderlich.");
            }
        }
        return type;
    }

    private static void validateField(ShopBlockType type, ShopBlockField field, JsonNode value,
                                      ShopBlockReferences references, boolean publishing) {
        String where = "„" + field.name() + "“ im Block „" + type.label + "“";
        switch (field.kind()) {
            case TEXT -> {
                if (!value.isTextual()) {
                    throw invalid(where);
                }
                if (value.asText().codePointCount(0, value.asText().length()) > field.maxLength()) {
                    throw new ApiException(400, "Das Feld " + where + " darf höchstens "
                            + field.maxLength() + " Zeichen lang sein.");
                }
            }
            case CHOICE -> {
                if (!value.isTextual() || !field.choices().contains(value.asText())) {
                    throw invalid(where);
                }
            }
            case BOOLEAN -> {
                if (!value.isBoolean()) {
                    throw invalid(where);
                }
            }
            case IMAGE -> {
                // Checked for drafts too: an asset id never goes stale (deletion refuses used assets),
                // and a foreign shop's asset must not even be stored.
                if (!value.canConvertToExactIntegral() || !references.isOwnAsset(value.asLong())) {
                    throw new ApiException(400, "Das Bild in " + where + " ist für diesen Shop nicht verfügbar.");
                }
            }
            case CATEGORY -> {
                if (!value.canConvertToExactIntegral()
                        || (publishing && !references.categoryExists(value.asLong()))) {
                    throw new ApiException(400, "Die Kategorie in " + where + " gibt es nicht.");
                }
            }
            case ARTICLES -> {
                if (!value.isArray() || value.size() > field.maxLength()) {
                    throw new ApiException(400, "In " + where + " sind höchstens "
                            + field.maxLength() + " Artikel erlaubt.");
                }
                for (JsonNode id : value) {
                    if (!id.canConvertToExactIntegral()
                            || (publishing && !references.isCatalogueArticle(id.asLong()))) {
                        throw new ApiException(400, "Ein Artikel in " + where + " ist für diesen Shop nicht freigeschaltet.");
                    }
                }
            }
            case SEGMENT -> {
                if (!value.isTextual() || (publishing && !references.isLinkablePage(value.asText()))) {
                    throw new ApiException(400, "Die verlinkte Seite in " + where + " gibt es nicht oder ist nicht online.");
                }
            }
            case COLORS -> ShopColors.validate(value, false, " im Block „" + type.label + "“");
        }
    }

    private static void rejectUnknown(JsonNode node, Set<String> allowed, String where) {
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String name = it.next();
            if (!allowed.contains(name)) {
                throw new ApiException(400, "Unbekanntes Feld „" + name + "“ " + where + ".");
            }
        }
    }

    private static boolean isEmpty(JsonNode value) {
        return value == null || value.isNull()
                || (value.isTextual() && value.asText().isBlank())
                || (value.isArray() && value.isEmpty());
    }

    private static boolean isUuid(JsonNode value) {
        if (value == null || !value.isTextual()) {
            return false;
        }
        try {
            return UUID.fromString(value.asText()).toString().equals(value.asText());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static ApiException invalid(String where) {
        return new ApiException(400, "Ungültiger Wert für " + where + ".");
    }
}
