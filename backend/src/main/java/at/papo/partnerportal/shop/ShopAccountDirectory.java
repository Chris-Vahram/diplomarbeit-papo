package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

// Removes a customer login from the papo-shop realm (shop-customer-accounts "Data export and
// account deletion"). Uses the confidential client papo-shop-admin, whose service account only
// holds manage-users/view-users of that realm; it can never touch the partnerportal realm.
@ApplicationScoped
public class ShopAccountDirectory {

    static final String UNAVAILABLE = "Das Konto kann gerade nicht gelöscht werden. Bitte versuchen Sie es später erneut.";
    private static final Pattern SUBJECT = Pattern.compile("^[0-9a-fA-F-]{36}$");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @ConfigProperty(name = "papo.shop.accounts.keycloak-url")
    String keycloakUrl;

    @ConfigProperty(name = "papo.shop.accounts.realm")
    String realm;

    @ConfigProperty(name = "papo.shop.accounts.admin-client-id")
    String clientId;

    @ConfigProperty(name = "papo.shop.accounts.admin-client-secret")
    Optional<String> clientSecret;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    // A login that is already gone (404) counts as deleted, so a retried deletion completes.
    public void deleteLogin(String subject) {
        if (subject == null || !SUBJECT.matcher(subject).matches()) {
            throw new ApiException(400, UNAVAILABLE);
        }
        try {
            HttpResponse<Void> response = http.send(HttpRequest.newBuilder(
                            URI.create(base() + "/admin/realms/" + realm + "/users/" + subject))
                    .timeout(TIMEOUT)
                    .header("Authorization", "Bearer " + adminToken())
                    .DELETE()
                    .build(), HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 204 && response.statusCode() != 404) {
                Log.warnf("papo-shop user deletion failed with HTTP %d", response.statusCode());
                throw new ApiException(503, UNAVAILABLE);
            }
        } catch (IOException e) {
            Log.warn("papo-shop user deletion failed: Keycloak not reachable");
            throw new ApiException(503, UNAVAILABLE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(503, UNAVAILABLE);
        }
    }

    private String adminToken() throws IOException, InterruptedException {
        String secret = clientSecret.filter(value -> !value.isBlank())
                .orElseThrow(() -> new ApiException(503, UNAVAILABLE));
        String form = "grant_type=client_credentials&client_id=" + encode(clientId) + "&client_secret=" + encode(secret);
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(
                        URI.create(base() + "/realms/" + realm + "/protocol/openid-connect/token"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            Log.warnf("papo-shop admin token request failed with HTTP %d", response.statusCode());
            throw new ApiException(503, UNAVAILABLE);
        }
        JsonNode token = objectMapper.readTree(response.body()).get("access_token");
        if (token == null || !token.isTextual()) {
            throw new ApiException(503, UNAVAILABLE);
        }
        return token.asText();
    }

    private String base() {
        return keycloakUrl.endsWith("/") ? keycloakUrl.substring(0, keycloakUrl.length() - 1) : keycloakUrl;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
