package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.AddressRequest;

import java.math.BigDecimal;

public record CompanyPartnerDto(
        Long id,
        String partnerNumber,
        String name,
        AddressRequest address,
        String email,
        String phone,
        String internalNote,
        BigDecimal commissionPercent,
        boolean active
) {
    public static CompanyPartnerDto from(Partner partner) {
        return new CompanyPartnerDto(partner.id, partner.partnerNumber, partner.name,
                AddressRequest.from(partner.address), partner.email, partner.phone,
                partner.internalNote, partner.commissionPercent, partner.active);
    }
}
