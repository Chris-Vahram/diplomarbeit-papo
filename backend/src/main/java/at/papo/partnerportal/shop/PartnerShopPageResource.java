package at.papo.partnerportal.shop;

import at.papo.partnerportal.security.CurrentPartner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Pages of the partner's own shop for the editor (shop-builder spec).
@Path("/api/partner/shop/pages")
@RolesAllowed("partner")
@Produces(MediaType.APPLICATION_JSON)
public class PartnerShopPageResource {

    @Inject
    CurrentPartner currentPartner;

    @Inject
    ShopEditorService editorService;

    @GET
    public ShopPageListDto list() {
        return editorService.pages(currentPartner.resolve());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageDetailDto create(ShopEditorRequests.CreatePage request) {
        return editorService.createPage(currentPartner.resolve(), ShopEditorRequests.require(request));
    }

    @PUT
    @Path("/order")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageListDto reorder(ShopEditorRequests.PageOrder request) {
        return editorService.reorder(currentPartner.resolve(), ShopEditorRequests.require(request));
    }

    @GET
    @Path("/{id}")
    public ShopPageDetailDto get(@PathParam("id") long id) {
        return editorService.page(currentPartner.resolve(), id);
    }

    @PUT
    @Path("/{id}/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageDto updateSettings(@PathParam("id") long id, ShopEditorRequests.PageSettings request) {
        return editorService.updateSettings(currentPartner.resolve(), id, ShopEditorRequests.require(request));
    }

    @PUT
    @Path("/{id}/draft")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageDto saveDraft(@PathParam("id") long id, ShopEditorRequests.Draft request) {
        return editorService.saveDraft(currentPartner.resolve(), id, ShopEditorRequests.require(request));
    }

    @POST
    @Path("/{id}/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageDto publish(@PathParam("id") long id, ShopEditorRequests.Version request) {
        return editorService.publish(currentPartner.resolve(), id, ShopEditorRequests.require(request).version());
    }

    @POST
    @Path("/{id}/unpublish")
    public ShopPageDto unpublish(@PathParam("id") long id) {
        return editorService.unpublish(currentPartner.resolve(), id);
    }

    @POST
    @Path("/{id}/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopPageDetailDto restore(@PathParam("id") long id, ShopEditorRequests.Version request) {
        return editorService.restore(currentPartner.resolve(), id, ShopEditorRequests.require(request).version());
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        editorService.delete(currentPartner.resolve(), id);
    }
}
