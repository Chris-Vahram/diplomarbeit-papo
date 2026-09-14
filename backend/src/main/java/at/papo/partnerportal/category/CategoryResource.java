package at.papo.partnerportal.category;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/company/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    @RolesAllowed("company")
    public List<CategoryDto> list() {
        return categoryService.list();
    }

    @POST
    @RolesAllowed("company")
    public CategoryDto create(@Valid CategoryCreateRequest request) {
        return categoryService.create(request);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("company")
    public CategoryDto update(@PathParam("id") Long id, @Valid CategoryUpdateRequest request) {
        return categoryService.update(id, request);
    }
}
