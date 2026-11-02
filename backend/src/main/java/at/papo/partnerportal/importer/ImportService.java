package at.papo.partnerportal.importer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

@ApplicationScoped
public class ImportService {

    private static final int SAMPLE_ROW_LIMIT = 5;

    public ImportPreviewDto preview(ImportDefinition definition, byte[] fileBytes, String filename) {
        ParsedFile parsed = ImportedFileParser.parse(fileBytes, filename);
        List<ImportFieldDto> fieldDtos = definition.fields().stream().map(ImportFieldDto::from).toList();
        return new ImportPreviewDto(fieldDtos, parsed.headers(), parsed.rows().stream().limit(SAMPLE_ROW_LIMIT).toList(),
                suggestMapping(definition.fields(), parsed.headers()), parsed.rows().size());
    }

    // Validates every row first (in-memory report), then persists the accepted
    // ones as a single transaction - rejected rows never touch the DB
    // (data-import spec: "without silently creating inconsistent or partially
    // corrupt data").
    @Transactional
    public ImportReportDto commit(ImportDefinition definition, byte[] fileBytes, String filename,
            Map<String, String> mapping) {
        return commit(definition, fileBytes, filename, mapping, UnaryOperator.identity());
    }

    // rowTransform runs on every mapped row before validation, e.g. the net-to-gross price
    // conversion of the article import, so the report shows the value that gets stored.
    @Transactional
    public ImportReportDto commit(ImportDefinition definition, byte[] fileBytes, String filename,
            Map<String, String> mapping, UnaryOperator<Map<String, String>> rowTransform) {
        ParsedFile parsed = ImportedFileParser.parse(fileBytes, filename);
        List<ImportRowResultDto> results = new ArrayList<>();
        Set<String> seenInFile = new HashSet<>();
        List<Map<String, String>> mappedRows = parsed.rows().stream()
                .map(fileRow -> rowTransform.apply(mapRow(definition.fields(), mapping, fileRow))).toList();
        definition.prepare(mappedRows);
        int rowNumber = 1;

        for (Map<String, String> mappedValues : mappedRows) {
            rowNumber++; // row 1 is the header
            List<String> errors = new ArrayList<>(definition.validateRow(mappedValues));
            errors.addAll(definition.validatePreparedRow(mappedValues));

            String identifier = definition.identifierValue(mappedValues);
            if (errors.isEmpty() && identifier != null && !identifier.isBlank()) {
                String normalizedIdentifier = identifier.toLowerCase();
                if (seenInFile.contains(normalizedIdentifier)) {
                    errors.add("Doppelter Eintrag innerhalb der Datei.");
                } else if (definition.existsInDb(identifier)) {
                    errors.add("Existiert bereits.");
                } else {
                    seenInFile.add(normalizedIdentifier);
                }
            }

            results.add(new ImportRowResultDto(rowNumber, errors.isEmpty(), mappedValues, errors));
        }

        for (ImportRowResultDto result : results) {
            if (result.accepted()) {
                definition.persist(result.data());
            }
        }

        long acceptedCount = results.stream().filter(ImportRowResultDto::accepted).count();
        return new ImportReportDto(results.size(), (int) acceptedCount, (int) (results.size() - acceptedCount), results);
    }

    private Map<String, String> mapRow(List<ImportField> fields, Map<String, String> mapping,
            Map<String, String> fileRow) {
        Map<String, String> mappedValues = new LinkedHashMap<>();
        for (ImportField field : fields) {
            String header = mapping.get(field.key());
            mappedValues.put(field.key(), header != null ? fileRow.getOrDefault(header, "") : "");
        }
        return mappedValues;
    }

    private Map<String, String> suggestMapping(List<ImportField> fields, List<String> headers) {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (ImportField field : fields) {
            for (String header : headers) {
                String normalizedHeader = normalize(header);
                boolean matches = normalize(field.key()).equals(normalizedHeader)
                        || normalize(field.label()).equals(normalizedHeader)
                        || field.aliases().stream().anyMatch(alias -> normalize(alias).equals(normalizedHeader));
                if (matches) {
                    mapping.put(field.key(), header);
                    break;
                }
            }
        }
        return mapping;
    }

    // Transliterate German special characters before stripping non-alphanumerics,
    // so e.g. header "Straße" matches alias "strasse" and "Verfügbar" matches
    // "verfuegbar" (data-import spec: Austrian client, umlaut-aware mapping).
    private String normalize(String value) {
        String transliterated = value.toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
        return transliterated.replaceAll("[^a-z0-9]", "");
    }
}
