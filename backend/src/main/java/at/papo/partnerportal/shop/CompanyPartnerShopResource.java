package at.papo.partnerportal.shop;

import at.papo.partnerportal.partner.PartnerService;
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

// Company manages each partner's shop: identifier, release, takedown with reason.
@Path("/api/company/partners/{partnerId}/shop")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class CompanyPartnerShopResource {

    @Inject
    PartnerService partnerService;

    @Inject
    PartnerShopService shopService;

    @GET
    public PartnerShopDto get(@PathParam("partnerId") Long partnerId) {
        return shopService.toDto(shopService.findOrCreate(partnerService.findOrThrow(partnerId)));
    }

    @PUT
    @Path("/kennung")
    @Consumes(MediaType.APPLICATION_JSON)
    public PartnerShopDto changeKennung(@PathParam("partnerId") Long partnerId, @Valid ShopKennungRequest request) {
        return shopService.changeKennung(partnerService.findOrThrow(partnerId), request.kennung());
    }

    @POST
    @Path("/release")
    public PartnerShopDto release(@PathParam("partnerId") Long partnerId) {
        return shopService.release(partnerService.findOrThrow(partnerId));
    }

    @POST
    @Path("/takedown")
    @Consumes(MediaType.APPLICATION_JSON)
    public PartnerShopDto takeOffline(@PathParam("partnerId") Long partnerId, @Valid ShopTakedownRequest request) {
        return shopService.takeOffline(partnerService.findOrThrow(partnerId), request.reason());
    }
}
