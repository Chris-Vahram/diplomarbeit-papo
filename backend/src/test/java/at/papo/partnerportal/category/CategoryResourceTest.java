package at.papo.partnerportal.category;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.restassured.response.Response;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CategoryResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyCanRenameDeactivateReactivateAndKeepExistingAssignment() {
        String token = companyToken();
        int categoryId = createCategory(token, "Verwaltung Kategorie");
        int productId = createProduct(token, "Verwaltung Artikel", categoryId);

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Umbenannte Kategorie\",\"active\":false}")
                .when().put("/api/company/categories/" + categoryId)
                .then().statusCode(200)
                .body("id", is(categoryId))
                .body("name", is("Umbenannte Kategorie"))
                .body("active", is(false));

        given().auth().oauth2(token)
                .when().get("/api/company/products/" + productId)
                .then().statusCode(200)
                .body("categoryId", is(categoryId))
                .body("category", is("Umbenannte Kategorie"));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Verwaltung Artikel geändert\",\"price\":2.00,\"categoryId\":" + categoryId + "}")
                .when().put("/api/company/products/" + productId)
                .then().statusCode(200)
                .body("categoryId", is(categoryId));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Neuer inaktiver Artikel\",\"price\":3.00,\"categoryId\":" + categoryId + "}")
                .when().post("/api/company/products")
                .then().statusCode(400)
                .body("message", containsString("deaktiviert"));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Umbenannte Kategorie\",\"active\":true}")
                .when().put("/api/company/categories/" + categoryId)
                .then().statusCode(200)
                .body("id", is(categoryId))
                .body("active", is(true));
    }

    @Test
    void categoryEndpointsRejectMissingAndWrongRolesAndInvalidUpdates() {
        String companyToken = companyToken();
        int categoryId = createCategory(companyToken, "Rollen Kategorie");
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        given().when().get("/api/company/categories").then().statusCode(401);
        given().contentType("application/json").body("{\"name\":\"Anonym\"}")
                .when().post("/api/company/categories").then().statusCode(401);
        given().contentType("application/json").body("{\"name\":\"Anonym\",\"active\":true}")
                .when().put("/api/company/categories/" + categoryId).then().statusCode(401);

        given().auth().oauth2(partnerToken).when().get("/api/company/categories").then().statusCode(403);
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"name\":\"Partner Kategorie\"}")
                .when().post("/api/company/categories").then().statusCode(403);
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"name\":\"Partner Kategorie\",\"active\":true}")
                .when().put("/api/company/categories/" + categoryId).then().statusCode(403);

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"name\":\"Rollen Kategorie\"}")
                .when().post("/api/company/categories")
                .then().statusCode(409);

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"name\":\"Unbekannte Kategorie\",\"active\":true}")
                .when().put("/api/company/categories/999999")
                .then().statusCode(404);
    }

    @Test
    void concurrentRenameAndAssignmentUseConsistentCategoryLockOrder() throws Exception {
        String token = companyToken();
        int categoryId = createCategory(token, "Parallele Kategorie");
        int productId = createProduct(token, "Paralleler Artikel", null);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Response> rename = executor.submit(() -> {
                start.await();
                return given().auth().oauth2(token).contentType("application/json")
                        .body("{\"name\":\"Parallele Kategorie neu\",\"active\":true}")
                        .when().put("/api/company/categories/" + categoryId).then().extract().response();
            });
            Future<Response> assign = executor.submit(() -> {
                start.await();
                return given().auth().oauth2(token).contentType("application/json")
                        .body("{\"name\":\"Paralleler Artikel\",\"price\":2.00,\"categoryId\":" + categoryId + "}")
                        .when().put("/api/company/products/" + productId).then().extract().response();
            });
            start.countDown();
            assertTrue(rename.get(10, TimeUnit.SECONDS).statusCode() == 200);
            assertTrue(assign.get(10, TimeUnit.SECONDS).statusCode() == 200);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentDeactivationAndNewAssignmentNeverBypassesInactiveRule() throws Exception {
        String token = companyToken();
        int categoryId = createCategory(token, "Sperrparallele Kategorie");
        int productId = createProduct(token, "Sperrparalleler Artikel", null);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Response> deactivate = executor.submit(() -> {
                start.await();
                return given().auth().oauth2(token).contentType("application/json")
                        .body("{\"name\":\"Sperrparallele Kategorie\",\"active\":false}")
                        .when().put("/api/company/categories/" + categoryId).then().extract().response();
            });
            Future<Response> assign = executor.submit(() -> {
                start.await();
                return given().auth().oauth2(token).contentType("application/json")
                        .body("{\"name\":\"Sperrparalleler Artikel\",\"price\":2.00,\"categoryId\":" + categoryId + "}")
                        .when().put("/api/company/products/" + productId).then().extract().response();
            });
            start.countDown();
            int deactivateStatus = deactivate.get(10, TimeUnit.SECONDS).statusCode();
            int assignStatus = assign.get(10, TimeUnit.SECONDS).statusCode();
            assertTrue(deactivateStatus == 200);
            assertTrue(assignStatus == 200 || assignStatus == 400);
        } finally {
            executor.shutdownNow();
        }
    }

    private String companyToken() {
        return tokens.tokenFor("company-demo", "company-demo");
    }

    private int createCategory(String token, String name) {
        return given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when().post("/api/company/categories")
                .then().statusCode(200).extract().path("id");
    }

    private int createProduct(String token, String name, Integer categoryId) {
        String category = categoryId == null ? "" : ",\"categoryId\":" + categoryId;
        return given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"" + name + "\",\"price\":1.00" + category + "}")
                .when().post("/api/company/products")
                .then().statusCode(200).extract().path("id");
    }
}
