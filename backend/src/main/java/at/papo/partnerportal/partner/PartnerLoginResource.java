package at.papo.partnerportal.partner;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/company/partners/{partnerId}/logins")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartnerLoginResource {

    @Inject
    PartnerLoginService partnerLoginService;

    @GET
    public List<PartnerLoginDto> list(@PathParam("partnerId") Long partnerId) {
        return partnerLoginService.list(partnerId);
    }

    @POST
    public PartnerLoginDto add(@PathParam("partnerId") Long partnerId, @Valid @NotNull(message = "Keycloak-Benutzer-ID ist erforderlich.") PartnerLoginRequest request) {
        return partnerLoginService.add(partnerId, request);
    }

    @DELETE
    @Path("/{loginId}")
    public void revoke(@PathParam("partnerId") Long partnerId, @PathParam("loginId") Long loginId) {
        partnerLoginService.revoke(partnerId, loginId);
    }
}
