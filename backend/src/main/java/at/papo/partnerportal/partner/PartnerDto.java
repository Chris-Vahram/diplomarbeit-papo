package at.papo.partnerportal.partner;

import java.math.BigDecimal;

public record PartnerDto(
        Long id,
        String partnerNumber,
        String name,
        String address,
        String email,
        String phone,
        BigDecimal commissionPercent,
        boolean active
) {
    public static PartnerDto from(Partner partner) {
        return new PartnerDto(partner.id, partner.partnerNumber, partner.name, partner.address,
                partner.email, partner.phone, partner.commissionPercent, partner.active);
    }
}
