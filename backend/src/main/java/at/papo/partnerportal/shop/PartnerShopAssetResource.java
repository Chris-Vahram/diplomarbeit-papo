package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.common.UploadLimits;
import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;

// Image library of the partner's own shop (shop-assets spec).
@Path("/api/partner/shop/assets")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerShopAssetResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    ShopAssetService assetService;

    @GET
    public ShopAssetListDto list() {
        return assetService.list(currentPartner.resolve());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ShopAssetDto upload(@RestForm("file") FileUpload file, @RestForm("rightsConfirmed") boolean rightsConfirmed)
            throws IOException {
        if (file == null) {
            throw new ApiException(400, "Bitte eine Bilddatei auswählen.");
        }
        UploadLimits.rejectIfLarger(file.uploadedFile(), UploadLimits.SHOP_ASSET_BYTES,
                "Das Bild ist zu groß (maximal 5 MB).");
        return assetService.upload(currentPartner.resolve(), Files.readAllBytes(file.uploadedFile()),
                file.fileName(), rightsConfirmed);
    }

    // Editor preview only; the public shop serves published images via /api/shop/<kennung>/assets.
    @GET
    @Path("/{id}")
    @Produces(MediaType.WILDCARD)
    public Response get(@PathParam("id") long id) {
        ShopAsset asset = assetService.find(currentPartner.resolve(), id);
        return Response.ok(asset.data)
                .type(asset.contentType)
                .header("Cache-Control", "private, max-age=300")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        assetService.delete(currentPartner.resolve(), id);
    }
}
