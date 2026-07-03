package at.papo.partnerportal.importer;

import java.util.List;
import java.util.Map;

public record ParsedFile(List<String> headers, List<Map<String, String>> rows) {
}
