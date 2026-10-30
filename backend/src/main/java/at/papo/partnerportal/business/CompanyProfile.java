package at.papo.partnerportal.business;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class CompanyProfile extends PanacheEntity {

    // The company is the tenant (add-company-tenancy). singletonKey is kept, nullable and unused,
    // only so a restored pre-tenancy image still finds company 1 ('CURRENT'); Release B drops it.
    public static final String CURRENT_KEY = "CURRENT";

    @Column(unique = true, length = 16)
    public String singletonKey;

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
