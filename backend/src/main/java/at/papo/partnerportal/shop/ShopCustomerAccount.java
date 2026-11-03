package at.papo.partnerportal.shop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

// Profile of an end-customer account (shop-customer-accounts spec). The identity itself lives in
// the separate Keycloak realm papo-shop; this row only holds what prefills the checkout. It is
// platform-wide: orders stay per partner shop, the profile is never shown to partners.
@Entity
@Table(name = "shop_customer_account", uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_customer_account_subject", columnNames = "subject"))
public class ShopCustomerAccount extends PanacheEntity {

    // OIDC "sub" of the papo-shop realm; never a client-supplied value.
    @Column(nullable = false, length = 64)
    public String subject;

    public String lastName;

    public String firstName;

    public String street;

    public String houseNumber;

    public String postalCode;

    public String city;

    public String country;

    // Contact address for orders; starts as the verified login e-mail and may differ from it.
    @Column(length = 254)
    public String email;

    @Column(length = 40)
    public String phone;

    @Column(nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public Instant updatedAt;
}
