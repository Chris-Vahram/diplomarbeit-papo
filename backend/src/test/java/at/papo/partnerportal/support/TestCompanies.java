package at.papo.partnerportal.support;

import at.papo.partnerportal.business.CompanyProfile;

// Test fixtures that persist owned rows directly must assign a company (add-company-tenancy):
// company 1 is the seeded 'CURRENT' profile of company-demo / partner-demo.
public final class TestCompanies {

    private TestCompanies() {
    }

    public static CompanyProfile one() {
        return CompanyProfile.find("singletonKey", CompanyProfile.CURRENT_KEY).firstResult();
    }

    public static CompanyProfile two() {
        return CompanyProfile.find("businessName", "Testfirma Zwei GmbH").firstResult();
    }
}
