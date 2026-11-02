package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.security.CurrentCompany;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CompanyProfileService {

    @Inject
    BusinessTaxIdentifierValidator taxIdentifierValidator;

    @Inject
    TradeRegisterIdentifierValidator tradeRegisterIdentifierValidator;

    @Inject
    CurrentCompany currentCompany;

    public CompanyProfileDto get() {
        return CompanyProfileDto.from(currentCompany.resolve());
    }

    @Transactional
    public CompanyProfileDto update(CompanyProfileRequest request) {
        CompanyProfile profile = currentCompany.resolve();
        profile.businessName = request.businessName().trim();
        profile.taxIdentifier = taxIdentifierValidator.normalize(request.taxIdentifier());
        profile.tradeRegisterIdentifier = tradeRegisterIdentifierValidator.normalize(request.tradeRegisterIdentifier());
        // Absent field keeps the address (older clients). Clearing stores "" rather than null:
        // null means "never set", which the Release A migration may fill from the legacy setting.
        if (request.orderNoticeEmail() != null) {
            profile.orderNoticeEmail = request.orderNoticeEmail().trim();
        }
        return CompanyProfileDto.from(profile);
    }

    public CompanyLegalTextsDto legalTexts() {
        return CompanyLegalTextsDto.from(currentCompany.resolve());
    }

    // Stored as typed; only surrounding blank lines go. Markup is never interpreted here or in
    // the shop renderer, so no sanitising is needed on the way in.
    @Transactional
    public CompanyLegalTextsDto updateLegalTexts(CompanyLegalTextsRequest request) {
        if (request == null) {
            throw new ApiException(400, "Bitte die Rechtstexte mitsenden.");
        }
        CompanyProfile profile = currentCompany.resolve();
        profile.legalImpressum = normalizeLegal(request.impressum());
        profile.legalAgb = normalizeLegal(request.agb());
        profile.legalDatenschutz = normalizeLegal(request.datenschutz());
        profile.legalWiderruf = normalizeLegal(request.widerruf());
        return CompanyLegalTextsDto.from(profile);
    }

    private static String normalizeLegal(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.replace("\r\n", "\n").strip();
    }
}
