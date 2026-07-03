package at.papo.partnerportal.order;

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

import java.util.List;

// Company-facing: sees all orders, advances status. Partners view status
// read-only via their own order list (order-management spec).
@Path("/api/company/orders")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyOrderResource {

    @Inject
    OrderService orderService;

    @GET
    public List<OrderDto> list() {
        return orderService.listAll();
    }

    @PUT
    @Path("/{id}/status")
    public OrderDto updateStatus(@PathParam("id") Long id, @Valid OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
