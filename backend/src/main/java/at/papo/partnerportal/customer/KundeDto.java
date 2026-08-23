package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.AddressRequest;

import java.time.LocalDate;

public record KundeDto(
        Long id,
        String lastName,
        String firstName,
        AddressRequest address,
        String email,
        String phone,
        LocalDate lastOrderDate
) {
    public static KundeDto from(Kunde kunde) {
        return from(kunde, null);
    }

    public static KundeDto from(Kunde kunde, LocalDate lastOrderDate) {
        return new KundeDto(kunde.id, kunde.lastName, kunde.firstName, AddressRequest.from(kunde.address),
                kunde.email, kunde.phone, lastOrderDate);
    }
}
