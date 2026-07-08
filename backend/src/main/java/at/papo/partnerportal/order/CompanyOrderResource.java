package at.papo.partnerportal.order;

import at.papo.partnerportal.export.CsvExportService;
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

// Company-facing: sees all orders, advances status. Partners view status
// read-only via their own order list (order-management spec).
@Path("/api/company/orders")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyOrderResource {

    @Inject
    OrderService orderService;

    @Inject
    CsvExportService csvExportService;

    @GET
    public List<OrderDto> list() {
        return orderService.listAll();
    }

    @GET
    @Path("/export")
    @Produces("text/csv;charset=UTF-8")
    public byte[] exportCsv() {
        return csvExportService.exportOrders();
    }

    // Manual order creation on a partner's behalf (order-management spec,
    // "Bestellung manuell anlegen").
    @POST
    public OrderDto create(@Valid CompanyOrderRequest request) {
        return orderService.createForPartnerId(request.partnerId(), request);
    }

    @PUT
    @Path("/{id}/status")
    public OrderDto updateStatus(@PathParam("id") Long id, @Valid OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @PUT
    @Path("/{id}/seen")
    public OrderDto markSeen(@PathParam("id") Long id) {
        return orderService.markSeen(id);
    }
}
