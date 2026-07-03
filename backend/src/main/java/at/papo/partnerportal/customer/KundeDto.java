package at.papo.partnerportal.customer;

public record KundeDto(
        Long id,
        String lastName,
        String firstName,
        String address,
        String email,
        String phone
) {
    public static KundeDto from(Kunde kunde) {
        return new KundeDto(kunde.id, kunde.lastName, kunde.firstName, kunde.address, kunde.email, kunde.phone);
    }
}
