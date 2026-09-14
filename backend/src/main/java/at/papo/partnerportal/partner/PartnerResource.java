package at.papo.partnerportal.partner;

import at.papo.partnerportal.customer.KundeDto;
import at.papo.partnerportal.customer.KundeService;
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

    @Inject
    KundeService kundeService;

    @GET
    public List<CompanyPartnerDto> list() {
        return partnerService.list();
    }

    @GET
    @Path("/{id}")
    public CompanyPartnerDto get(@PathParam("id") Long id) {
        return partnerService.get(id);
    }

    // For the company's manual order-creation flow: that partner's own Endkunden,
    // scoped exactly like the partner's own /partner/customers (design.md).
    @GET
    @Path("/{id}/customers")
    public List<KundeDto> customers(@PathParam("id") Long id) {
        return kundeService.listForPartner(partnerService.findOrThrow(id));
    }

    @POST
    public CompanyPartnerDto create(@Valid PartnerRequest request) {
        return partnerService.create(request);
    }

    @PUT
    @Path("/{id}")
    public CompanyPartnerDto update(@PathParam("id") Long id, @Valid PartnerRequest request) {
        return partnerService.update(id, request);
    }
}
