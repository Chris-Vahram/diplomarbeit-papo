package at.papo.partnerportal.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class BusinessTaxIdentifier {

    @Column(name = "tax_country", length = 2)
    public String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_identifier_type", length = 32)
    public BusinessTaxIdentifierType type;

    @Column(name = "tax_identifier_value", length = 64)
    public String value;

    public BusinessTaxIdentifier() {
    }

    public BusinessTaxIdentifier(String country, BusinessTaxIdentifierType type, String value) {
        this.country = country;
        this.type = type;
        this.value = value;
    }
}
