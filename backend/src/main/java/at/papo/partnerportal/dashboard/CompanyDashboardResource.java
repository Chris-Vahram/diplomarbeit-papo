package at.papo.partnerportal.dashboard;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/company/dashboard")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class CompanyDashboardResource {

    @Inject
    DashboardService dashboardService;

    @GET
    public CompanyDashboardDto get() {
        return dashboardService.buildForCompany();
    }
}
