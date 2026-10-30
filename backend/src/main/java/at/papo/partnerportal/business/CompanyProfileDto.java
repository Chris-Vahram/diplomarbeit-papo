package at.papo.partnerportal.business;

public record CompanyProfileDto(Long id, String businessName, BusinessTaxIdentifierDto taxIdentifier,
        TradeRegisterIdentifierDto tradeRegisterIdentifier, String orderNoticeEmail) {

    public static CompanyProfileDto from(CompanyProfile profile) {
        return new CompanyProfileDto(profile.id, profile.businessName,
                BusinessTaxIdentifierDto.from(profile.taxIdentifier),
                TradeRegisterIdentifierDto.from(profile.tradeRegisterIdentifier), profile.orderNoticeEmail);
    }
}
