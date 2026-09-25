package at.papo.partnerportal.product;

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

    @GET
    public List<ProductImageDto> list() {
        return productImageService.list();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProductImageDto upload(@RestForm("file") FileUpload file) throws IOException {
        UploadLimits.rejectIfLarger(file.uploadedFile(), UploadLimits.PRODUCT_IMAGE_BYTES,
                "Bild ist zu groß (maximal 2 MiB erlaubt).");
        return productImageService.upload(Files.readAllBytes(file.uploadedFile()), file.fileName());
    }

    // Accepted risk (2026-07-09 security review): any authenticated partner can read any
    // image id, independent of product enablement - ids are sequential/enumerable. Not
    // fixed because product images are non-sensitive (gallery assets, not partner data);
    // revisit with an enablement check if image confidentiality ever matters.
    @GET
    @Path("/{id}")
    @RolesAllowed({ "company", "partner" })
    @Produces(MediaType.WILDCARD)
    public Response get(@PathParam("id") Long id) {
        ProductImage image = productImageService.findOrThrow(id);
        return Response.ok(image.data)
                .type(image.contentType)
                .header("Cache-Control", "public, max-age=31536000, immutable")
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
