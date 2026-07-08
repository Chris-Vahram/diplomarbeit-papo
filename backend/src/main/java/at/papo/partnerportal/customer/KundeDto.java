package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.AddressRequest;

public record KundeDto(
        Long id,
        String lastName,
        String firstName,
        AddressRequest address,
        String email,
        String phone
) {
    public static KundeDto from(Kunde kunde) {
        return new KundeDto(kunde.id, kunde.lastName, kunde.firstName, AddressRequest.from(kunde.address),
                kunde.email, kunde.phone);
    }
}
