package at.papo.partnerportal.business;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/company/profile")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyProfileResource {

    @Inject
    CompanyProfileService companyProfileService;

    @GET
    public CompanyProfileDto get() {
        return companyProfileService.get();
    }

    @PUT
    public CompanyProfileDto update(@Valid CompanyProfileRequest request) {
        return companyProfileService.update(request);
    }
}
