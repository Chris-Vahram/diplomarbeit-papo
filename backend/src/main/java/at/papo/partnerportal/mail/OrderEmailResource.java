package at.papo.partnerportal.mail;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Company-only delivery status per order (order-emails spec "Delivery status for the company").
@Path("/api/company/orders/{orderId}/emails")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class OrderEmailResource {

    @Inject
    OrderEmailService orderEmailService;

    @GET
    public List<OrderEmailDto> list(@PathParam("orderId") Long orderId) {
        return orderEmailService.listForOrder(orderId);
    }

    @POST
    @Path("/{emailId}/retry")
    public OrderEmailDto retry(@PathParam("orderId") Long orderId, @PathParam("emailId") Long emailId) {
        return orderEmailService.retry(orderId, emailId);
    }
}
