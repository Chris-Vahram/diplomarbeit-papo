package at.papo.partnerportal.shop;

import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

// Rate limit (429) and noindex for every public shop response (shop-public-access spec).
@Provider
@PublicShopEndpoint
public class PublicShopFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    ShopRateLimiter rateLimiter;

    @ConfigProperty(name = "papo.shop.indexing-enabled")
    boolean indexingEnabled;

    @Context
    HttpServerRequest httpRequest;

    @Override
    public void filter(ContainerRequestContext request) {
        if (!rateLimiter.tryAcquire(clientIp(request.getHeaderString("X-Forwarded-For"), httpRequest))) {
            request.abortWith(Response.status(429)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("message", "Zu viele Anfragen. Bitte kurz warten."))
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        // Indexing is only ever allowed for a reachable shop (200) on a deployment that opts in.
        if (!indexingEnabled || response.getStatus() != 200) {
            response.getHeaders().putSingle("X-Robots-Tag", "noindex");
        }
    }

    // In prod the backend is reachable only through Caddy, which appends the client address
    // as the right-most X-Forwarded-For entry; earlier entries are client-controlled.
    // Also used by the stricter inquiry limit (ShopInquiryService).
    static String clientIp(String forwarded, HttpServerRequest httpRequest) {
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].strip();
        }
        return httpRequest.remoteAddress() == null ? "unknown" : httpRequest.remoteAddress().hostAddress();
    }
}
