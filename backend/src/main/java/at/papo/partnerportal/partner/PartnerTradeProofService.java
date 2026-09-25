package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.TradeRegisterIdentifierRequest;
import at.papo.partnerportal.business.TradeRegisterIdentifierValidator;
import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class PartnerTradeProofService {

    @Inject
    EntityManager entityManager;

    @Inject
    TradeRegisterIdentifierValidator registerIdentifierValidator;

    @Transactional
    public PartnerTradeProofDto replace(Partner owner, TradeRegisterIdentifierRequest registerRequest,
            byte[] data, String filename, String declaredContentType) {
        String contentType = PartnerTradeProofValidator.validate(data, filename, declaredContentType);
        var registerIdentifier = registerIdentifierValidator.normalize(registerRequest);

        Partner lockedOwner = entityManager.find(Partner.class, owner.id, LockModeType.PESSIMISTIC_WRITE);
        if (lockedOwner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        if (!lockedOwner.active) {
            throw new ApiException(403, "Ihr Zugang wurde deaktiviert. Bitte wenden Sie sich an die Firma.");
        }
        PartnerTradeProof proof = PartnerTradeProof.find("partner", lockedOwner).firstResult();
        if (proof == null) {
            proof = new PartnerTradeProof();
            proof.partner = lockedOwner;
        }
        proof.registerIdentifier = registerIdentifier;
        proof.version = UUID.randomUUID().toString();
        proof.data = data;
        proof.contentType = contentType;
        proof.filename = safeFilename(filename);
        proof.sizeBytes = data.length;
        proof.uploadedAt = Instant.now();
        proof.status = PartnerTradeProofStatus.PENDING;
        proof.reviewedAt = null;
        proof.rejectionReason = null;
        if (proof.id == null) {
            proof.persist();
        }
        return PartnerTradeProofDto.from(proof);
    }

    public PartnerTradeProof find(Partner owner) {
        return PartnerTradeProof.find("partner.id", owner.id).firstResult();
    }

    public PartnerTradeProofDto status(Partner owner) {
        return PartnerTradeProofDto.from(find(owner));
    }

    @Transactional
    public PartnerTradeProofDownload download(Partner owner) {
        PartnerTradeProof proof = find(owner);
        if (proof == null) {
            throw new ApiException(404, "Nachweis nicht gefunden.");
        }
        return new PartnerTradeProofDownload(proof.data, proof.contentType, proof.filename);
    }

    @Transactional
    public PartnerTradeProofDto review(Partner owner, PartnerTradeProofReviewRequest request) {
        Partner lockedOwner = entityManager.find(Partner.class, owner.id, LockModeType.PESSIMISTIC_WRITE);
        if (lockedOwner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        PartnerTradeProof proof = PartnerTradeProof.find("partner.id", lockedOwner.id).firstResult();
        if (proof == null) {
            throw new ApiException(404, "Nachweis nicht gefunden.");
        }
        if (!proof.version.equals(request.version())) {
            throw new ApiException(409, "Der Nachweis wurde inzwischen ersetzt. Bitte erneut laden.");
        }
        if (request.status() != PartnerTradeProofStatus.ACCEPTED
                && request.status() != PartnerTradeProofStatus.REJECTED) {
            throw new ApiException(400, "Prüfstatus muss akzeptiert oder abgelehnt sein.");
        }
        proof.status = request.status();
        proof.reviewedAt = Instant.now();
        proof.rejectionReason = request.status() == PartnerTradeProofStatus.REJECTED
                && request.rejectionReason() != null && !request.rejectionReason().isBlank()
                        ? request.rejectionReason().trim() : null;
        return PartnerTradeProofDto.from(proof);
    }

    private String safeFilename(String filename) {
        String candidate = filename == null ? "" : filename.replace('\\', '/');
        int slash = candidate.lastIndexOf('/');
        candidate = slash >= 0 ? candidate.substring(slash + 1) : candidate;
        candidate = candidate.replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        if (candidate.isEmpty()) {
            candidate = "nachweis";
        }
        if (candidate.length() > 255) {
            candidate = candidate.substring(0, 255);
        }
        return candidate;
    }
}
