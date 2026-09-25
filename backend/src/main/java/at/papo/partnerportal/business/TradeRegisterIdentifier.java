package at.papo.partnerportal.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class TradeRegisterIdentifier {

    @Column(length = 2)
    public String country;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    public TradeRegisterIdentifierType type;

    @Column(length = 64)
    public String value;

    public TradeRegisterIdentifier() {
    }

    public TradeRegisterIdentifier(String country, TradeRegisterIdentifierType type, String value) {
        this.country = country;
        this.type = type;
        this.value = value;
    }
}
