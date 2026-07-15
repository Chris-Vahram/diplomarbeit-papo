package at.papo.partnerportal.partner;

import java.util.Locale;

// Canonical form of a Partner.partnerNumber: trimmed, upper-case (Locale.ROOT). Every write path
// (manual create/update in PartnerService, CSV import in PartnerImportDefinition) must agree on
// this one form, or the case-sensitive DB unique constraint stops preventing case-variant
// duplicates and CurrentPartner.resolve()'s exact-match login lookup stops finding a match.
public final class PartnerNumber {

    private PartnerNumber() {
    }

    public static String canonicalize(String partnerNumber) {
        return partnerNumber.trim().toUpperCase(Locale.ROOT);
    }
}
