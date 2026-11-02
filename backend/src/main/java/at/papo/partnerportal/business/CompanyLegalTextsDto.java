package at.papo.partnerportal.business;

import java.util.List;

// missing: texts the shops currently show as placeholder, in footer order.
public record CompanyLegalTextsDto(String impressum, String agb, String datenschutz, String widerruf,
        List<LegalText> missing) {

    public static CompanyLegalTextsDto from(CompanyProfile profile) {
        return new CompanyLegalTextsDto(profile.legalImpressum, profile.legalAgb, profile.legalDatenschutz,
                profile.legalWiderruf, LegalText.missing(profile));
    }
}
