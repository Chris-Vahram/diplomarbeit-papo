package at.papo.partnerportal.business;

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
}
