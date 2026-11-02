package at.papo.partnerportal.business;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// The four shop legal texts the company maintains in its profile (shop-legal spec). The segment
// is the shop system page that shows the text; the order is the footer order.
public enum LegalText {
    IMPRESSUM("impressum", "Impressum"),
    AGB("agb", "AGB"),
    DATENSCHUTZ("datenschutz", "Datenschutz"),
    WIDERRUF("widerruf", "Widerruf");

    public static final int MAX_LENGTH = 30_000;
    public static final String PLACEHOLDER_MARK = "Platzhalter – kein gültiger Rechtstext";

    public final String segment;
    public final String title;

    LegalText(String segment, String title) {
        this.segment = segment;
        this.title = title;
    }

    public static Optional<LegalText> bySegment(String segment) {
        return Arrays.stream(values()).filter(text -> text.segment.equals(segment)).findFirst();
    }

    public String of(CompanyProfile profile) {
        return switch (this) {
            case IMPRESSUM -> profile.legalImpressum;
            case AGB -> profile.legalAgb;
            case DATENSCHUTZ -> profile.legalDatenschutz;
            case WIDERRUF -> profile.legalWiderruf;
        };
    }

    // A missing text is shown as a placeholder; both count as "not a valid legal text".
    // A partner without a company (legacy rows before tenancy migration) has no texts at all.
    public static List<LegalText> missing(CompanyProfile profile) {
        if (profile == null) {
            return List.of(values());
        }
        return Arrays.stream(values()).filter(text -> isBlank(text.of(profile))).toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
