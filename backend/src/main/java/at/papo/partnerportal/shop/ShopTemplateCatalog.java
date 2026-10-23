package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

// Template catalogue (shop-templates spec, design.md Decision 1): shop-templates/index.json lists
// the templates, each stored as <id>.json in the page-draft format plus an optional "theme".
// Adding a template is a data change only. Entries are checked once at startup with the same
// validators as a publish; an invalid entry is logged and left out, never fatal.
@ApplicationScoped
@Startup
public class ShopTemplateCatalog {

    static final String DIR = "shop-templates/";

    private static final Logger LOG = Logger.getLogger(ShopTemplateCatalog.class);
    private static final Pattern ID = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");
    private static final Set<String> FILE_FIELDS = Set.of("blocks", "theme");
    private static final Map<String, ShopPageSlot> PAGES = Map.of(
            "startseite", ShopPageSlot.HOME,
            "artikel", ShopPageSlot.ARTICLES,
            "artikeldetail", ShopPageSlot.ARTICLE_DETAIL);
    static final String FREE_PAGE = "frei";

    // A template belongs to no shop: no own images, articles or categories, and buttons may only
    // lead to pages every shop has online.
    static final ShopBlockReferences NO_SHOP_DATA = new ShopBlockReferences() {
        @Override
        public boolean isOwnAsset(long assetId) {
            return false;
        }

        @Override
        public boolean categoryExists(long categoryId) {
            return false;
        }

        @Override
        public boolean isCatalogueArticle(long articleId) {
            return false;
        }

        @Override
        public boolean isLinkablePage(String segment) {
            return segment.equals(ShopPageSlot.HOME.segment) || segment.equals(ShopPageSlot.ARTICLES.segment);
        }
    };

    @Inject
    ObjectMapper objectMapper;

    private List<ShopTemplate> templates = List.of();

    @PostConstruct
    void load() {
        templates = load(objectMapper, DIR);
        LOG.infof("%d Shop-Vorlagen geladen.", templates.size());
    }

    public List<ShopTemplate> all() {
        return templates;
    }

    // slot null = free page.
    public Optional<ShopTemplate> find(String id, ShopPageSlot slot) {
        return templates.stream().filter(template -> template.id().equals(id) && slot(template) == slot).findFirst();
    }

    static ShopPageSlot slot(ShopTemplate template) {
        return PAGES.get(template.page());
    }

    static List<ShopTemplate> load(ObjectMapper mapper, String dir) {
        JsonNode index;
        try {
            index = read(mapper, dir + "index.json");
        } catch (IOException | RuntimeException exception) {
            LOG.errorf("Shop-Vorlagen: %sindex.json nicht lesbar: %s", dir, exception.getMessage());
            return List.of();
        }
        if (!index.isArray()) {
            LOG.errorf("Shop-Vorlagen: %sindex.json muss eine Liste sein.", dir);
            return List.of();
        }
        List<ShopTemplate> loaded = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode entry : index) {
            String id = entry.path("id").asText("");
            try {
                ShopTemplate template = parse(mapper, dir, entry);
                if (!ids.add(template.id())) {
                    throw new IllegalArgumentException("ID doppelt vergeben.");
                }
                loaded.add(template);
            } catch (IOException | RuntimeException exception) {
                LOG.errorf("Shop-Vorlage „%s“ wird nicht angeboten: %s", id, exception.getMessage());
            }
        }
        return List.copyOf(loaded);
    }

    private static ShopTemplate parse(ObjectMapper mapper, String dir, JsonNode entry) throws IOException {
        String id = requireText(entry, "id");
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Ungültige ID.");
        }
        String page = requireText(entry, "page");
        if (!page.equals(FREE_PAGE) && !PAGES.containsKey(page)) {
            throw new IllegalArgumentException("Unbekannte Seitenart „" + page + "“.");
        }
        JsonNode file = read(mapper, dir + id + ".json");
        if (!file.isObject()) {
            throw new IllegalArgumentException("Die Vorlage ist kein JSON-Objekt.");
        }
        file.fieldNames().forEachRemaining(field -> {
            if (!FILE_FIELDS.contains(field)) {
                throw new IllegalArgumentException("Unbekanntes Feld „" + field + "“.");
            }
        });
        JsonNode theme = file.get("theme");
        if (theme != null) {
            ShopThemeValidator.validate(theme);
        }
        ShopTemplate template = new ShopTemplate(id, page, requireText(entry, "name"),
                requireText(entry, "description"), requireText(entry, "thumbnail"), file.path("blocks"), theme);
        ShopBlockValidator.validatePage(ShopPageService.withBlockIds(template.content()), PAGES.get(page),
                NO_SHOP_DATA, true);
        return template;
    }

    private static JsonNode read(ObjectMapper mapper, String path) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Datei " + path + " fehlt.");
            }
            return mapper.readTree(in);
        }
    }

    private static String requireText(JsonNode entry, String field) {
        JsonNode value = entry.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Feld „" + field + "“ fehlt im Verzeichnis.");
        }
        return value.asText();
    }
}
