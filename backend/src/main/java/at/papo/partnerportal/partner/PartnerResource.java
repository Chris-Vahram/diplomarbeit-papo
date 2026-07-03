package at.papo.partnerportal.partner;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/company/partners")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartnerResource {

    @Inject
    PartnerService partnerService;

    @GET
    public List<PartnerDto> list() {
        return partnerService.list();
    }

    @GET
    @Path("/{id}")
    public PartnerDto get(@PathParam("id") Long id) {
        return partnerService.get(id);
    }

    @POST
    public PartnerDto create(@Valid PartnerRequest request) {
        return partnerService.create(request);
    }

    @PUT
    @Path("/{id}")
    public PartnerDto update(@PathParam("id") Long id, @Valid PartnerRequest request) {
        return partnerService.update(id, request);
    }
}
