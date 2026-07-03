package at.papo.partnerportal.enablement;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

// Company manages which products a partner may see/order (product-enablement spec).
@Path("/api/company/partners/{partnerId}/products")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class EnablementResource {

    @Inject
    EnablementService enablementService;

    @GET
    public List<EnablementDto> list(@PathParam("partnerId") Long partnerId) {
        return enablementService.listForPartner(partnerId);
    }

    @PUT
    @Path("/{productId}")
    public Response enable(@PathParam("partnerId") Long partnerId, @PathParam("productId") Long productId) {
        enablementService.enable(partnerId, productId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{productId}")
    public Response disable(@PathParam("partnerId") Long partnerId, @PathParam("productId") Long productId) {
        enablementService.disable(partnerId, productId);
        return Response.noContent().build();
    }
}
