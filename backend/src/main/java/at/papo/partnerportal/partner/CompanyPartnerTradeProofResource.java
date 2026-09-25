package at.papo.partnerportal.partner;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/company/partners/{partnerId}/trade-proof")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyPartnerTradeProofResource {

    @Inject
    PartnerService partnerService;

    @Inject
    PartnerTradeProofService proofService;

    @GET
    public PartnerTradeProofDto status(@PathParam("partnerId") Long partnerId) {
        return proofService.status(partnerService.findOrThrow(partnerId));
    }

    @GET
    @Path("/download")
    @Produces(MediaType.WILDCARD)
    public Response download(@PathParam("partnerId") Long partnerId) {
        return PartnerTradeProofResource.downloadResponse(
                proofService.download(partnerService.findOrThrow(partnerId)));
    }

    @PUT
    @Path("/review")
    public PartnerTradeProofDto review(@PathParam("partnerId") Long partnerId,
            @Valid PartnerTradeProofReviewRequest request) {
        return proofService.review(partnerService.findOrThrow(partnerId), request);
    }
}
