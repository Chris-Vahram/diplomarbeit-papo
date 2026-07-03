package at.papo.partnerportal.enablement;

import at.papo.partnerportal.product.ProductDto;
import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Partner-facing read of its own enabled products only (product-enablement spec,
// "Restricted product visibility"). Scoped via CurrentPartner — never a client-supplied id.
@Path("/api/partner/products")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerProductResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    EnablementService enablementService;

    @GET
    public List<ProductDto> list() {
        return enablementService.listEnabledProducts(currentPartner.resolve());
    }
}
