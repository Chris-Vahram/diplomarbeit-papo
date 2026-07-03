package at.papo.partnerportal.product;

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

// Company-only: product price/description are centrally controlled (FR-18).
// Partners read enabled products via product-enablement (Phase 2), not this resource.
@Path("/api/company/products")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    public List<ProductDto> list() {
        return productService.list();
    }

    @GET
    @Path("/{id}")
    public ProductDto get(@PathParam("id") Long id) {
        return productService.get(id);
    }

    @POST
    public ProductDto create(@Valid ProductRequest request) {
        return productService.create(request);
    }

    @PUT
    @Path("/{id}")
    public ProductDto update(@PathParam("id") Long id, @Valid ProductRequest request) {
        return productService.update(id, request);
    }
}
