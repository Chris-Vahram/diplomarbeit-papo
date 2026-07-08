package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.AddressRequest;

import java.math.BigDecimal;

public record PartnerDto(
        Long id,
        String partnerNumber,
        String name,
        AddressRequest address,
        String email,
        String phone,
        BigDecimal commissionPercent,
        boolean active,
        String keycloakUsername
) {
    public static PartnerDto from(Partner partner) {
        return new PartnerDto(partner.id, partner.partnerNumber, partner.name, AddressRequest.from(partner.address),
                partner.email, partner.phone, partner.commissionPercent, partner.active,
                partner.keycloakUsername);
    }
}
