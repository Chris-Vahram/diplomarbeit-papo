package at.papo.partnerportal.business;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class CompanyProfile extends PanacheEntity {

    // The company is the tenant (add-company-tenancy). The legacy column singletonkey stays in
    // migrated databases (nullable, company 1 keeps 'CURRENT') only for a restored Release A image.

    @Column(nullable = false, length = 255)
    public String businessName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "tax_country", nullable = false, length = 2)),
            @AttributeOverride(name = "type", column = @Column(name = "tax_identifier_type", nullable = false, length = 32)),
            @AttributeOverride(name = "value", column = @Column(name = "tax_identifier_value", nullable = false, length = 64))
    })
    public BusinessTaxIdentifier taxIdentifier;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "register_country", length = 2)),
            @AttributeOverride(name = "type", column = @Column(name = "register_type", length = 32)),
            @AttributeOverride(name = "value", column = @Column(name = "register_value", length = 64))
    })
    public TradeRegisterIdentifier tradeRegisterIdentifier;

    // Recipient of the company's order notices. null = never set (migration may copy the legacy
    // setting), "" = deliberately cleared; both skip the company mail (NO_RECIPIENT).
    @Column(length = 254)
    public String orderNoticeEmail;
}
