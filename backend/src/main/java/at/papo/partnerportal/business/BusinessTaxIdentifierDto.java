package at.papo.partnerportal.business;

public record BusinessTaxIdentifierDto(String country, BusinessTaxIdentifierType type, String value) {

    public static BusinessTaxIdentifierDto from(BusinessTaxIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return new BusinessTaxIdentifierDto(identifier.country, identifier.type, identifier.value);
    }
}
