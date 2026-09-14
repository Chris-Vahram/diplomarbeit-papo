package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CompanyProfileService {

    @Inject
    BusinessTaxIdentifierValidator taxIdentifierValidator;

    public CompanyProfileDto get() {
        return CompanyProfileDto.from(findCurrent());
    }

    @Transactional
    public CompanyProfileDto update(CompanyProfileRequest request) {
        CompanyProfile profile = findCurrent();
        profile.businessName = request.businessName().trim();
        profile.taxIdentifier = taxIdentifierValidator.normalize(request.taxIdentifier());
        return CompanyProfileDto.from(profile);
    }

    public CompanyProfile findCurrent() {
        CompanyProfile profile = CompanyProfile.find("singletonKey", CompanyProfile.CURRENT_KEY).firstResult();
        if (profile == null) {
            throw new ApiException(404, "Firmenprofil ist noch nicht angelegt.");
        }
        return profile;
    }
}
