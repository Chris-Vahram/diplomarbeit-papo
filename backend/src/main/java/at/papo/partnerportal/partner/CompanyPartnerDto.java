package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.BusinessTaxIdentifierDto;
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
        boolean active,
        BusinessTaxIdentifierDto taxIdentifier,
        PartnerTradeProofDto tradeProof
) {
    public static CompanyPartnerDto from(Partner partner) {
        PartnerTradeProof proof = PartnerTradeProof.find("partner.id", partner.id).firstResult();
        return from(partner, PartnerTradeProofDto.from(proof));
    }

    public static CompanyPartnerDto from(Partner partner, PartnerTradeProofDto tradeProof) {
        return new CompanyPartnerDto(partner.id, partner.partnerNumber, partner.name,
                AddressRequest.from(partner.address), partner.email, partner.phone,
                partner.internalNote, partner.commissionPercent, partner.active,
                BusinessTaxIdentifierDto.from(partner.taxIdentifier),
                tradeProof);
    }
}
