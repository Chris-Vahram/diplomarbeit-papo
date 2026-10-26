package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Bundled CC0 sample photos of the template catalogue, for the editor's demo shop and the
// template thumbnails (shop-templates spec "Bundled sample images"). Public like the shop: they
// contain no shop data, and only listed names are served.
@Path("/api/shop-templates/images")
@PermitAll
@PublicShopEndpoint
public class ShopTemplateImageResource {

    @Inject
    ShopTemplateCatalog templateCatalog;

    @GET
    @Path("/{name}")
    @Produces(MediaType.WILDCARD)
    public Response image(@PathParam("name") String name) {
        ShopTemplateImage image = templateCatalog.image(name)
                .orElseThrow(() -> new ApiException(404, "Bild nicht gefunden."));
        return Response.ok(templateCatalog.imageBytes(image))
                .type("image/webp")
                .header("Cache-Control", "public, max-age=86400")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }
}
