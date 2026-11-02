package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// Partner's own shop ("Mein Webshop"): status, theme draft, templates and preview data. The identifier
// and release stay company decisions.
@Path("/api/partner/shop")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerShopResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopPageService pageService;

    @Inject
    ShopEditorService editorService;

    @Inject
    ShopCatalogService catalogService;

    @Inject
    ShopTemplateCatalog templateCatalog;

    @Inject
    ShopStatisticsService statisticsService;

    @GET
    public PartnerShopDto get() {
        return shopService.toDto(shopService.findOrCreate(currentPartner.resolve()));
    }

    @GET
    @Path("/theme")
    public ShopThemeDto theme() {
        return editorService.theme(currentPartner.resolve());
    }

    @PUT
    @Path("/theme/draft")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopThemeDto saveThemeDraft(ShopEditorRequests.ThemeDraft request) {
        return editorService.saveThemeDraft(currentPartner.resolve(), ShopEditorRequests.require(request));
    }

    @POST
    @Path("/theme/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopThemeDto publishTheme(ShopEditorRequests.Version request) {
        return editorService.publishTheme(currentPartner.resolve(), ShopEditorRequests.require(request).version());
    }

    // First-time setup "Shop einrichten" (shop-templates spec).
    @POST
    @Path("/setup")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageListDto setup(ShopEditorRequests.Setup request) {
        return editorService.setup(currentPartner.resolve(), ShopEditorRequests.require(request));
    }

    // Template catalogue for the builder's picker and the "Neue Seite" dialog (shop-templates spec).
    @GET
    @Path("/templates")
    public List<ShopTemplate> templates() {
        currentPartner.resolve();
        return templateCatalog.all();
    }

    // Picker: the template prepared for this shop (sample photos copied into its images).
    @POST
    @Path("/templates/{id}/content")
    public ShopTemplateContentDto templateContent(@PathParam("id") String id) {
        return editorService.templateContent(currentPartner.resolve(), id);
    }

    @GET
    @Path("/preview")
    public ShopPreviewDto preview() {
        PartnerShop shop = shopService.findOrCreate(currentPartner.resolve());
        Partner partner = shop.partner;
        return new ShopPreviewDto(PublicShopDto.from(shop), PublicNavigationDto.of(shop, pageService),
                catalogService.articles(partner).stream().map(PublicArticleDto::forPreview).toList(),
                catalogService.categories(partner));
    }

    // "Webshop-Statistik" (shop-statistics spec): own shop only; from/to as YYYY-MM-DD,
    // default the current month.
    @GET
    @Path("/statistics")
    public ShopStatisticsDto statistics(@QueryParam("from") String from, @QueryParam("to") String to) {
        return statisticsService.statistics(shopService.findOrCreate(currentPartner.resolve()), date(from), date(to));
    }

    private static LocalDate date(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException invalid) {
            throw new ApiException(400, "Bitte einen gültigen Zeitraum wählen.");
        }
    }
}
