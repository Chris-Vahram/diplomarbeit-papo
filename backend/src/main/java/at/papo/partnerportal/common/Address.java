package at.papo.partnerportal.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Shared structured address for Kunde and Partner (street/houseNumber/postalCode/city/country).
// Mandatory-ness is enforced at the request-record level (@NotBlank), not here.
@Embeddable
public class Address {

    @Column(nullable = false)
    public String street;

    @Column(nullable = false)
    public String houseNumber;

    @Column(nullable = false)
    public String postalCode;

    @Column(nullable = false)
    public String city;

    @Column(nullable = false)
    public String country;

    public Address() {
    }

    public Address(String street, String houseNumber, String postalCode, String city, String country) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public String toDisplayString() {
        return street + " " + houseNumber + ", " + postalCode + " " + city + ", " + country;
    }
}
