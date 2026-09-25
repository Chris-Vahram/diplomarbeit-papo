package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.TradeRegisterIdentifierDto;

import java.time.Instant;

public record PartnerTradeProofDto(
        boolean present,
        String version,
        TradeRegisterIdentifierDto registerIdentifier,
        String filename,
        long sizeBytes,
        Instant uploadedAt,
        PartnerTradeProofStatus status,
        Instant reviewedAt,
        String rejectionReason
) {
    public static PartnerTradeProofDto from(PartnerTradeProof proof) {
        if (proof == null) {
            return new PartnerTradeProofDto(false, null, null, null, 0, null, null, null, null);
        }
        return new PartnerTradeProofDto(true, proof.version,
                TradeRegisterIdentifierDto.from(proof.registerIdentifier), proof.filename, proof.sizeBytes,
                proof.uploadedAt, proof.status, proof.reviewedAt, proof.rejectionReason);
    }
}
