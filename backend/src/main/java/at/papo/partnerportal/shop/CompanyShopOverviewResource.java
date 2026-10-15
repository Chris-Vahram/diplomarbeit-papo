package at.papo.partnerportal.shop;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Shop status per partner for the company partner table ("Webshop" column).
@Path("/api/company/shops")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class CompanyShopOverviewResource {

    @Inject
    PartnerShopService shopService;

    @GET
    public List<CompanyShopOverviewDto> list() {
        return shopService.overview();
    }
}
