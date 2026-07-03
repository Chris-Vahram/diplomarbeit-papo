package at.papo.partnerportal.partner;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Partner's own profile: limited self-edit of contact data only (FR-08).
// Scoped server-side via CurrentPartner — never trusts a client-supplied id.
@Path("/api/partner/profile")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartnerSelfResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    PartnerService partnerService;

    @GET
    public PartnerDto get() {
        return PartnerDto.from(currentPartner.resolve());
    }

    @PUT
    public PartnerDto update(@Valid PartnerContactRequest request) {
        return partnerService.updateOwnContactData(currentPartner.resolve(), request);
    }
}
