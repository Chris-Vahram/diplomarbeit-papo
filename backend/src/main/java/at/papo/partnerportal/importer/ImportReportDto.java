package at.papo.partnerportal.importer;

import java.util.List;

public record ImportReportDto(int totalRows, int acceptedCount, int rejectedCount, List<ImportRowResultDto> rows) {
}
