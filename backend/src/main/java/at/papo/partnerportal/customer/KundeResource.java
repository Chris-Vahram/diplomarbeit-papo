package at.papo.partnerportal.customer;

import at.papo.partnerportal.security.CurrentPartner;
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

// Partner-facing only - a partner selects/creates its own Kunden while ordering
// (customer-management spec). No company-facing management endpoint: the company
// sees Kunde data only indirectly, through order detail (design.md).
@Path("/api/partner/customers")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KundeResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    KundeService kundeService;

    @GET
    public List<KundeDto> list() {
        return kundeService.listForPartner(currentPartner.resolve());
    }

    @POST
    public KundeDto create(@Valid KundeRequest request) {
        return kundeService.create(currentPartner.resolve(), request);
    }

    @PUT
    @Path("/{id}")
    public KundeDto update(@PathParam("id") Long id, @Valid KundeRequest request) {
        return kundeService.update(currentPartner.resolve(), id, request);
    }
}
