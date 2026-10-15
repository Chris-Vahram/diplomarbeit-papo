package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// PAPO's only unauthenticated API (shop-public-access spec). The partner is resolved from
// the identifier alone; unknown and offline shops share one 404 so existence is not revealed.
@Path("/api/shop/{kennung}")
@PermitAll
@PublicShopEndpoint
@Produces(MediaType.APPLICATION_JSON)
public class PublicShopResource {

    @Inject
    PartnerShopService shopService;

    @GET
    public PublicShopDto get(@PathParam("kennung") String kennung) {
        return shopService.findOnline(kennung)
                .map(PublicShopDto::from)
                .orElseThrow(() -> new ApiException(404, PartnerShopService.NOT_REACHABLE));
    }
}
