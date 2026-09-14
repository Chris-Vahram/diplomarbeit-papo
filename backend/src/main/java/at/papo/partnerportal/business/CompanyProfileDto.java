package at.papo.partnerportal.business;

public record CompanyProfileDto(Long id, String businessName, BusinessTaxIdentifierDto taxIdentifier) {

    public static CompanyProfileDto from(CompanyProfile profile) {
        return new CompanyProfileDto(profile.id, profile.businessName,
                BusinessTaxIdentifierDto.from(profile.taxIdentifier));
    }
}
