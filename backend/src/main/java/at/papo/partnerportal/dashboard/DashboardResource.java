package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/partner/dashboard")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    DashboardService dashboardService;

    @GET
    public PartnerDashboardDto get() {
        return dashboardService.buildForPartner(currentPartner.resolve());
    }
}
