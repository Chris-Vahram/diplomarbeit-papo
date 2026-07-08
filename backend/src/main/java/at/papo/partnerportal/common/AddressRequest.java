package at.papo.partnerportal.common;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank(message = "Straße ist erforderlich.") String street,
        @NotBlank(message = "Hausnummer ist erforderlich.") String houseNumber,
        @NotBlank(message = "Postleitzahl ist erforderlich.") String postalCode,
        @NotBlank(message = "Ort ist erforderlich.") String city,
        @NotBlank(message = "Land ist erforderlich.") String country
) {
    public Address toEntity() {
        return new Address(street, houseNumber, postalCode, city, country);
    }

    public static AddressRequest from(Address address) {
        return new AddressRequest(address.street, address.houseNumber, address.postalCode,
                address.city, address.country);
    }
}
