package at.papo.partnerportal.shop;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Partner's own shop status ("Mein Webshop"). Read-only: the identifier is company-controlled.
@Path("/api/partner/shop")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerShopResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    PartnerShopService shopService;

    @GET
    public PartnerShopDto get() {
        return shopService.toDto(shopService.findOrCreate(currentPartner.resolve()));
    }
}
