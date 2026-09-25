package at.papo.partnerportal.business;

public record TradeRegisterIdentifierDto(String country, TradeRegisterIdentifierType type, String value) {

    public static TradeRegisterIdentifierDto from(TradeRegisterIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return new TradeRegisterIdentifierDto(identifier.country, identifier.type, identifier.value);
    }
}
