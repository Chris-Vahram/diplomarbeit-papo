package at.papo.partnerportal.product;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class ProductImageResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    private static byte[] fakeJpeg() {
        byte[] data = new byte[32];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xD8;
        data[2] = (byte) 0xFF;
        return data;
    }

    private static byte[] fakeWebp() {
        byte[] data = new byte[32];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, data, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, data, 8, 4);
        return data;
    }

    @Test
    void companyUploadsValidPng() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] png = DemoPngGenerator.solidColorPng(4, 4, 200, 50, 50);

        given().auth().oauth2(token)
                .multiPart("file", "test.png", png, "image/png")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .body("contentType", is("image/png"))
                .body("url", startsWith("/product-images/"));
    }

    @Test
    void companyUploadsValidJpegAndWebp() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .multiPart("file", "test.jpg", fakeJpeg(), "image/jpeg")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .body("contentType", is("image/jpeg"));

        given().auth().oauth2(token)
                .multiPart("file", "test.webp", fakeWebp(), "image/webp")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .body("contentType", is("image/webp"));
    }

    // The multipart-declared content type is attacker-controlled; the backend must sniff
    // magic bytes and ignore what the client claims (design.md "Validation lives server-side").
    @Test
    void declaredContentTypeIsIgnoredInFavourOfSniffedBytes() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .multiPart("file", "spoofed.png", fakeJpeg(), "image/png")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .body("contentType", is("image/jpeg"));
    }

    @Test
    void svgUploadIsRejected() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "evil.svg", svg, "image/svg+xml")
                .when().post("/api/product-images")
                .then().statusCode(400);
    }

    @Test
    void nonImageUploadIsRejected() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] text = "not an image".getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "notes.txt", text, "text/plain")
                .when().post("/api/product-images")
                .then().statusCode(400);
    }

    @Test
    void oversizeUploadIsRejected() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] tooLarge = new byte[2 * 1024 * 1024 + 100];
        tooLarge[0] = (byte) 0xFF;
        tooLarge[1] = (byte) 0xD8;
        tooLarge[2] = (byte) 0xFF;

        given().auth().oauth2(token)
                .multiPart("file", "huge.jpg", tooLarge, "image/jpeg")
                .when().post("/api/product-images")
                .then().statusCode(413);
    }

    @Test
    void partnerCannotUploadOrListGallery() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        byte[] png = DemoPngGenerator.solidColorPng(2, 2, 10, 10, 10);

        given().auth().oauth2(token)
                .multiPart("file", "test.png", png, "image/png")
                .when().post("/api/product-images")
                .then().statusCode(403);

        given().auth().oauth2(token)
                .when().get("/api/product-images")
                .then().statusCode(403);
    }

    @Test
    void companyListsGallery() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] png = DemoPngGenerator.solidColorPng(2, 2, 20, 20, 20);
        given().auth().oauth2(token)
                .multiPart("file", "test.png", png, "image/png")
                .when().post("/api/product-images")
                .then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/api/product-images")
                .then().statusCode(200)
                .body("size()", not(0));
    }

    @Test
    void bothRolesCanLoadAnUploadedImageByServedContentType() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        byte[] png = DemoPngGenerator.solidColorPng(4, 4, 5, 5, 5);
        int id = given().auth().oauth2(companyToken)
                .multiPart("file", "test.png", png, "image/png")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(companyToken)
                .when().get("/api/product-images/" + id)
                .then().statusCode(200)
                .contentType("image/png");

        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partnerToken)
                .when().get("/api/product-images/" + id)
                .then().statusCode(200)
                .contentType("image/png");
    }

    @Test
    void unknownImageIdReturns404() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .when().get("/api/product-images/999999999")
                .then().statusCode(404);
    }

    // RestAssured's .multiPart(name, filename, bytes, mime) helper mis-encodes non-ASCII
    // filenames client-side (verified: the server decodes a correctly UTF-8-encoded
    // Content-Disposition filename perfectly - this is a RestAssured limitation, not a
    // product bug). Building the multipart body by hand exercises the real wire format a
    // browser sends and proves the server-side handling this test actually cares about.
    @Test
    void umlautFilenameIsPreservedAndImageStillServedById() throws Exception {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] png = DemoPngGenerator.solidColorPng(3, 3, 1, 2, 3);
        String boundary = "----UmlautTestBoundary";
        String filename = "Prüfsiegel-Größe.png";

        java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        body.write("Content-Type: image/png\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        body.write(png);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        int id = given().auth().oauth2(token)
                .contentType("multipart/form-data; boundary=" + boundary)
                .body(body.toByteArray())
                .when().post("/api/product-images")
                .then().statusCode(200)
                .body("filename", is(filename))
                .extract().path("id");

        given().auth().oauth2(token)
                .when().get("/api/product-images/" + id)
                .then().statusCode(200)
                .contentType("image/png");
    }
}
