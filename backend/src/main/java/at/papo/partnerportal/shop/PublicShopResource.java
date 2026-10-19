package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.business.CompanyProfileService;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.product.ProductImage;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

// PAPO's only unauthenticated API (shop-public-access spec). The partner is resolved from
// the identifier alone; unknown and offline shops share one 404 so existence is not revealed.
@Path("/api/shop/{kennung}")
@PermitAll
@PublicShopEndpoint
@Produces(MediaType.APPLICATION_JSON)
public class PublicShopResource {

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopPageService pageService;

    @Inject
    ShopCatalogService catalogService;

    @Inject
    CompanyProfileService companyProfileService;

    @GET
    public PublicShopDto get(@PathParam("kennung") String kennung) {
        return PublicShopDto.from(onlineShop(kennung));
    }

    @GET
    @Path("/theme")
    public JsonNode theme(@PathParam("kennung") String kennung) {
        return pageService.parse(pageService.theme(onlineShop(kennung)).publishedTheme);
    }

    @GET
    @Path("/navigation")
    public PublicNavigationDto navigation(@PathParam("kennung") String kennung) {
        PartnerShop shop = onlineShop(kennung);
        CompanyProfile seller = companyProfileService.findCurrent();
        return new PublicNavigationDto(
                pageService.navigation(shop).stream().map(PublicNavigationDto.Item::from).toList(),
                pageService.legalPages(shop).stream().map(PublicNavigationDto.Item::from).toList(),
                seller == null ? null : seller.businessName);
    }

    @GET
    @Path("/pages/{segment}")
    public PublicPageDto page(@PathParam("kennung") String kennung, @PathParam("segment") String segment) {
        ShopPage page = pageService.onlinePage(onlineShop(kennung), segment)
                .orElseThrow(() -> new ApiException(404, "Diese Seite gibt es nicht."));
        return new PublicPageDto(page.segment, page.menuName, page.slot,
                pageService.parse(page.publishedContent).get("blocks"));
    }

    @GET
    @Path("/articles")
    public List<PublicArticleDto> articles(@PathParam("kennung") String kennung) {
        PartnerShop shop = onlineShop(kennung);
        return catalogService.articles(shop.partner).stream()
                .map(product -> PublicArticleDto.from(product, shop.kennung))
                .toList();
    }

    @GET
    @Path("/articles/{id}")
    public PublicArticleDto article(@PathParam("kennung") String kennung, @PathParam("id") long id) {
        PartnerShop shop = onlineShop(kennung);
        return catalogService.article(shop.partner, id)
                .map(product -> PublicArticleDto.from(product, shop.kennung))
                .orElseThrow(() -> new ApiException(404, "Dieser Artikel ist nicht verfügbar."));
    }

    @GET
    @Path("/categories")
    public List<PublicCategoryDto> categories(@PathParam("kennung") String kennung) {
        return catalogService.categories(onlineShop(kennung).partner);
    }

    // Only images of this shop's catalogue articles; any other id is a 404 like an unknown one.
    // The stored content type was verified from the file bytes at upload (ProductImageValidator).
    @GET
    @Path("/images/{id}")
    @Produces(MediaType.WILDCARD)
    public Response image(@PathParam("kennung") String kennung, @PathParam("id") long id) {
        ProductImage image = catalogService.publicImage(onlineShop(kennung).partner, id)
                .orElseThrow(() -> new ApiException(404, "Bild nicht gefunden."));
        return Response.ok(image.data)
                .type(image.contentType)
                // private + short: an image stops being public when the shop goes offline or the
                // article is disabled, so shared caches must not keep serving it.
                .header("Cache-Control", "private, max-age=300")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    private PartnerShop onlineShop(String kennung) {
        return shopService.findOnline(kennung)
                .orElseThrow(() -> new ApiException(404, PartnerShopService.NOT_REACHABLE));
    }
}
