package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import io.quarkus.security.Authenticated;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;
import java.util.List;

// Customer account API (shop-customer-accounts spec). Only this namespace is served by the OIDC
// tenant papo-shop (application.properties tenant-paths); portal tokens fail validation here and
// papo-shop tokens fail everywhere else. Customers have no roles, hence @Authenticated.
@Path("/api/shop-customer")
@Authenticated
@PublicShopEndpoint
@Produces(MediaType.APPLICATION_JSON)
public class ShopCustomerResource {

    static final String NOT_VERIFIED = "Bitte bestätigen Sie zuerst Ihre E-Mail-Adresse.";

    @Inject
    JsonWebToken jwt;

    @Inject
    ShopCustomerService customerService;

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopOrderService orderService;

    @Context
    HttpServerRequest httpRequest;

    @ConfigProperty(name = "papo.shop.accounts-enabled")
    boolean accountsEnabled;

    @GET
    @Path("/profile")
    public ShopCustomerDtos.Profile profile() {
        return customerService.profile(subject(), loginEmail());
    }

    @PUT
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public ShopCustomerDtos.Profile updateProfile(ShopCustomerDtos.ProfileRequest request) {
        return customerService.updateProfile(subject(), loginEmail(), request);
    }

    @GET
    @Path("/shops/{kennung}/orders")
    public List<ShopCustomerDtos.CustomerOrder> orders(@PathParam("kennung") String kennung) {
        String subject = subject();
        return customerService.orders(subject, onlineShop(kennung));
    }

    @GET
    @Path("/shops/{kennung}/orders/{id}")
    public ShopCustomerDtos.CustomerOrder order(@PathParam("kennung") String kennung, @PathParam("id") long id) {
        String subject = subject();
        return customerService.order(subject, onlineShop(kennung), id);
    }

    // Checkout of a logged-in customer: the guest checkout with the account subject attached.
    @POST
    @Path("/shops/{kennung}/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response placeOrder(@PathParam("kennung") String kennung, @HeaderParam("X-Forwarded-For") String forwarded,
                               ShopOrderRequest request) {
        String subject = subject();
        ShopOrderConfirmationDto confirmation = orderService.submit(onlineShop(kennung), request,
                PublicShopFilter.clientIp(forwarded, httpRequest), subject);
        return Response.status(201).entity(confirmation).build();
    }

    @GET
    @Path("/export")
    public Response export() {
        ShopCustomerDtos.Export export = customerService.export(subject(), loginEmail());
        return Response.ok(export)
                .header("Content-Disposition", "attachment; filename=\"kundenkonto-" + LocalDate.now() + ".json\"")
                .header("Cache-Control", "no-store")
                .build();
    }

    @DELETE
    @Path("/account")
    public Response delete() {
        customerService.delete(subject());
        return Response.noContent().build();
    }

    // Keycloak refuses logins of unverified accounts already; this is the server-side backstop.
    private String subject() {
        if (!accountsEnabled) {
            throw new ApiException(404, "Kundenkonten sind derzeit nicht verfügbar.");
        }
        Object verified = jwt.getClaim("email_verified");
        if (!Boolean.TRUE.equals(verified) && !"true".equals(String.valueOf(verified))) {
            throw new ApiException(403, NOT_VERIFIED);
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ApiException(401, "Bitte melden Sie sich erneut an.");
        }
        return subject;
    }

    private String loginEmail() {
        return jwt.getClaim("email");
    }

    private PartnerShop onlineShop(String kennung) {
        return shopService.findOnline(kennung)
                .orElseThrow(() -> new ApiException(404, PartnerShopService.NOT_REACHABLE));
    }
}
