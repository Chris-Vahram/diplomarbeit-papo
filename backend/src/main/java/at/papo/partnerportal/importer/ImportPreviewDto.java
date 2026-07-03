package at.papo.partnerportal.importer;

import java.util.List;
import java.util.Map;

public record ImportPreviewDto(
        List<ImportFieldDto> fields,
        List<String> headers,
        List<Map<String, String>> sampleRows,
        Map<String, String> suggestedMapping,
        int totalRows
) {
}
