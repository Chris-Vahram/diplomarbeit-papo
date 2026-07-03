package at.papo.partnerportal.importer;

import java.util.List;
import java.util.Map;

public record ImportRowResultDto(int rowNumber, boolean accepted, Map<String, String> data, List<String> errors) {
}
