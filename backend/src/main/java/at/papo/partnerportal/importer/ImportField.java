package at.papo.partnerportal.importer;

import java.util.List;

// One importable system field. Aliases are alternate header spellings used to
// suggest a default column mapping (normalized: lowercase, non-alphanumerics
// stripped) - see ImportService.suggestMapping.
public record ImportField(String key, String label, boolean required, List<String> aliases) {
}
