package at.papo.partnerportal.order;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Partner-facing: create an order for oneself, see only one's own orders
// (order-management spec). Scoped via CurrentPartner - never a client-supplied id.
@Path("/api/partner/orders")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    OrderService orderService;

    @GET
    public List<OrderDto> list() {
        return orderService.listForPartner(currentPartner.resolve());
    }

    @POST
    public OrderDto create(@Valid OrderRequest request) {
        return orderService.createForPartner(currentPartner.resolve(), request);
    }
}
