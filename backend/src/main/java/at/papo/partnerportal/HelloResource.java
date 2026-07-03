package at.papo.partnerportal;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.inject.Inject;

@Path("/hello")
public class HelloResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("/company")
    @RolesAllowed("company")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloCompany() {
        return "Hello company, " + jwt.getName();
    }

    @GET
    @Path("/partner")
    @RolesAllowed("partner")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPartner() {
        return "Hello partner, " + jwt.getName();
    }

    @GET
    @Path("/secured")
    @Authenticated
    @Produces(MediaType.TEXT_PLAIN)
    public String helloSecured() {
        return "Hello authenticated user, " + jwt.getName();
    }
}
