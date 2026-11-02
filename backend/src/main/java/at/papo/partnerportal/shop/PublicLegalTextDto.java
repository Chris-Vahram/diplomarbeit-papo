package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.business.LegalText;

// One legal system page of a shop. text is restricted Markdown, rendered escaped by the shop.
// placeholder = the company has not provided this text; the shop marks the page visibly.
public record PublicLegalTextDto(String segment, String title, String text, boolean placeholder) {

    static PublicLegalTextDto of(LegalText kind, CompanyProfile company) {
        String text = company == null ? null : kind.of(company);
        if (text == null || text.isBlank()) {
            return new PublicLegalTextDto(kind.segment, kind.title, placeholderText(kind, company), true);
        }
        return new PublicLegalTextDto(kind.segment, kind.title, text, false);
    }

    private static String placeholderText(LegalText kind, CompanyProfile company) {
        String seller = company == null ? "das Unternehmen" : company.businessName;
        return "# " + kind.title + "\n\n" + LegalText.PLACEHOLDER_MARK + ".\n\n"
                + "Dieser Text wird von " + seller + " im Firmenprofil hinterlegt. "
                + "Bis dahin gilt dieser Shop nur als Testumgebung.";
    }
}
