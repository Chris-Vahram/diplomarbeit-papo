package at.papo.partnerportal.business;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class CompanyProfile extends PanacheEntity {

    public static final String CURRENT_KEY = "CURRENT";

    @Column(nullable = false, unique = true, length = 16)
    public String singletonKey;

    @Column(nullable = false, length = 255)
    public String businessName;

    @Embedded
    public BusinessTaxIdentifier taxIdentifier;
}
