package at.papo.partnerportal.importer;

import java.util.List;
import java.util.Map;

// One implementation per importable entity (Partner, Product). Shared parsing/
// mapping/report flow lives in ImportService; entity-specific validation,
// duplicate identification and persistence live here.
public interface ImportDefinition {

    List<ImportField> fields();

    // mappedValues: system field key -> raw file cell value (may be blank).
    List<String> validateRow(Map<String, String> mappedValues);

    // Value used for duplicate detection, both within the file and against the DB.
    String identifierValue(Map<String, String> mappedValues);

    boolean existsInDb(String identifierValue);

    void persist(Map<String, String> mappedValues);
}
