package at.papo.partnerportal.export;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/api/company/commissions")
@RolesAllowed("company")
public class CompanyCommissionResource {

    @Inject
    CsvExportService csvExportService;

    @GET
    @Path("/export")
    @Produces("text/csv;charset=UTF-8")
    public byte[] exportCsv() {
        return csvExportService.exportCommissions();
    }
}
