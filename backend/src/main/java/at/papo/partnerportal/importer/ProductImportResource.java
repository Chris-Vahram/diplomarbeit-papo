package at.papo.partnerportal.importer;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Path("/api/company/import/products")
@RolesAllowed("company")
@Produces(MediaType.APPLICATION_JSON)
public class ProductImportResource {

    @Inject
    ImportService importService;

    @Inject
    ProductImportDefinition definition;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/preview")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ImportPreviewDto preview(@RestForm("file") FileUpload file) throws IOException {
        return importService.preview(definition, Files.readAllBytes(file.uploadedFile()), file.fileName());
    }

    @POST
    @Path("/commit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ImportReportDto commit(@RestForm("file") FileUpload file, @RestForm("mapping") String mappingJson)
            throws IOException {
        Map<String, String> mapping = parseMapping(mappingJson);
        return importService.commit(definition, Files.readAllBytes(file.uploadedFile()), file.fileName(), mapping);
    }

    private Map<String, String> parseMapping(String mappingJson) {
        try {
            return objectMapper.readValue(mappingJson, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new ApiException(400, "Spaltenzuordnung ist ungültig.");
        }
    }
}
