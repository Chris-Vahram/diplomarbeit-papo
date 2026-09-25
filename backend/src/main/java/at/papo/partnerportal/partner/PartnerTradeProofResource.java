package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.TradeRegisterIdentifierRequest;
import at.papo.partnerportal.business.TradeRegisterIdentifierType;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.common.UploadLimits;
import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

@Path("/api/partner/trade-proof")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerTradeProofResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    PartnerTradeProofService proofService;

    @GET
    public PartnerTradeProofDto status() {
        return proofService.status(currentPartner.resolve());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public PartnerTradeProofDto upload(@RestForm("registerCountry") String country,
            @RestForm("registerType") String type, @RestForm("registerValue") String value,
            @RestForm("file") FileUpload file) throws IOException {
        if (file == null) {
            throw new ApiException(400, "Nachweisdatei ist erforderlich.");
        }
        UploadLimits.rejectIfLarger(file.uploadedFile(), PartnerTradeProofValidator.MAX_SIZE_BYTES,
                "Nachweisdatei ist zu groß (maximal 10 MiB erlaubt).");
        return proofService.replace(currentPartner.resolve(),
                new TradeRegisterIdentifierRequest(country, parseType(type), value),
                Files.readAllBytes(file.uploadedFile()), file.fileName(), file.contentType());
    }

    @GET
    @Path("/download")
    @Produces(MediaType.WILDCARD)
    public Response download() {
        return downloadResponse(proofService.download(currentPartner.resolve()));
    }

    static Response downloadResponse(PartnerTradeProofDownload download) {
        return Response.ok(download.data())
                .type(download.contentType())
                .header("Cache-Control", "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Disposition", "attachment; filename=\"" + download.filename() + "\"")
                .build();
    }

    private TradeRegisterIdentifierType parseType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TradeRegisterIdentifierType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(400, "Registerart ist ungültig.");
        }
    }
}
