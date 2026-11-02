package at.papo.partnerportal.shop;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Partner inbox "Anfragen" (shop-inquiries spec). Only the owning partner; the company has
// no access (role check), another partner gets the same 404 as for a missing inquiry.
@Path("/api/partner/shop/inquiries")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerShopInquiryResource {

    public record DoneRequest(boolean done) {
    }

    @Inject
    CurrentPartner currentPartner;

    @Inject
    ShopInquiryService inquiryService;

    @GET
    public List<ShopInquiryDto> list() {
        return inquiryService.list(currentPartner.resolve());
    }

    // Navigation badge, refreshed like the company's unseen-order count.
    @GET
    @Path("/unread-count")
    public long unreadCount() {
        return inquiryService.unreadCount(currentPartner.resolve());
    }

    @POST
    @Path("/{id}/read")
    public ShopInquiryDto markRead(@PathParam("id") long id) {
        return inquiryService.markRead(currentPartner.resolve(), id);
    }

    @PUT
    @Path("/{id}/done")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopInquiryDto setDone(@PathParam("id") long id, DoneRequest request) {
        return inquiryService.setDone(currentPartner.resolve(), id, request != null && request.done());
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        inquiryService.delete(currentPartner.resolve(), id);
    }
}
