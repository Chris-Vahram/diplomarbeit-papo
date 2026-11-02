package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.product.ProductImage;
import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

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
    ShopAssetService assetService;

    @Inject
    ShopInquiryService inquiryService;

    @Context
    HttpServerRequest httpRequest;

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
        return PublicNavigationDto.of(onlineShop(kennung), pageService);
    }

    @GET
    @Path("/pages/{segment}")
    public PublicPageDto page(@PathParam("kennung") String kennung, @PathParam("segment") String segment) {
        ShopPage page = pageService.onlinePage(onlineShop(kennung), segment)
                .orElseThrow(() -> new ApiException(404, "Diese Seite gibt es nicht."));
        return new PublicPageDto(page.segment, page.menuName, page.slot, page.seoTitle, page.seoDescription,
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

    // Shop's own uploads, only while a published online page shows them (shop-assets spec).
    @GET
    @Path("/assets/{id}")
    @Produces(MediaType.WILDCARD)
    public Response asset(@PathParam("kennung") String kennung, @PathParam("id") long id) {
        ShopAsset asset = assetService.publicAsset(onlineShop(kennung), id)
                .orElseThrow(() -> new ApiException(404, "Bild nicht gefunden."));
        return Response.ok(asset.data)
                .type(asset.contentType)
                .header("Cache-Control", "private, max-age=300")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    // Signed render timestamp for the inquiry form's minimum fill time (shop-inquiries spec).
    @GET
    @Path("/inquiries/token")
    public Map<String, String> inquiryToken(@PathParam("kennung") String kennung) {
        return Map.of("token", inquiryService.issueToken(onlineShop(kennung)));
    }

    @POST
    @Path("/inquiries")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response inquiry(@PathParam("kennung") String kennung, @HeaderParam("X-Forwarded-For") String forwarded,
                            ShopInquiryRequest request) {
        if (request == null) {
            throw new ApiException(400, ShopInquiryService.RETRY);
        }
        inquiryService.submit(onlineShop(kennung), request, PublicShopFilter.clientIp(forwarded, httpRequest));
        return Response.status(201).build();
    }

    private PartnerShop onlineShop(String kennung) {
        return shopService.findOnline(kennung)
                .orElseThrow(() -> new ApiException(404, PartnerShopService.NOT_REACHABLE));
    }
}
