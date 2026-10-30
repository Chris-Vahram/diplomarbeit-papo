package at.papo.partnerportal.product;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.security.CurrentCompany;
import at.papo.partnerportal.security.CurrentPartner;
import io.quarkus.security.identity.SecurityIdentity;
import at.papo.partnerportal.common.UploadLimits;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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
import java.util.List;

// Company-only: uploading and browsing the reusable product-image gallery is a
// product-management action (see product-images spec). Byte-serving (GET /{id}) is
// readable by both roles - partners load thumbnails of their enabled products too
// (see design.md "Serve bytes id-addressed"). Frontend <img> tags can't carry the
// Keycloak bearer token, so callers fetch the bytes with Authorization and build an
// object URL (frontend/src/lib/product-images.js) rather than using a plain <img src>.
@Path("/api/product-images")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class ProductImageResource {

    @Inject
    ProductImageService productImageService;

    @Inject
    CurrentCompany currentCompany;

    @Inject
    CurrentPartner currentPartner;

    @Inject
    SecurityIdentity identity;

    @GET
    public List<ProductImageDto> list() {
        return productImageService.list(currentCompany.resolve());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProductImageDto upload(@RestForm("file") FileUpload file) throws IOException {
        UploadLimits.rejectIfLarger(file.uploadedFile(), UploadLimits.PRODUCT_IMAGE_BYTES,
                "Bild ist zu groß (maximal 2 MiB erlaubt).");
        return productImageService.upload(currentCompany.resolve(), Files.readAllBytes(file.uploadedFile()),
                file.fileName());
    }

    // Readable only within the owning company (add-company-tenancy); a partner sees all images
    // of its own company, independent of enablement (gallery assets, not partner data).
    // private, no-store: browser caches ignore Authorization, a cached image would survive a
    // login switch to another company.
    @GET
    @Path("/{id}")
    @RolesAllowed({ "company", "partner" })
    @Produces(MediaType.WILDCARD)
    public Response get(@PathParam("id") Long id) {
        CompanyProfile company = identity.hasRole("company") ? currentCompany.resolve()
                : currentPartner.resolve().company;
        ProductImage image = productImageService.findOrThrow(company, id);
        return Response.ok(image.data)
                .type(image.contentType)
                .header("Cache-Control", "private, no-store")
                .header("Content-Disposition", "inline; filename=\"product-image-" + id + extensionFor(image.contentType) + "\"")
                .build();
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
