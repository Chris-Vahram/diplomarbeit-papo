package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.BusinessTaxIdentifierValidator;
import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PartnerService {

    @Inject
    BusinessTaxIdentifierValidator taxIdentifierValidator;

    public List<CompanyPartnerDto> list() {
        List<Partner> partners = Partner.listAll();
        if (partners.isEmpty()) {
            return List.of();
        }
        List<Long> partnerIds = partners.stream().map(partner -> partner.id).toList();
        Map<Long, PartnerTradeProofDto> proofs = PartnerTradeProof.<PartnerTradeProof>find("partner.id in ?1", partnerIds)
                .list().stream()
                .collect(Collectors.toMap(proof -> proof.partner.id, PartnerTradeProofDto::from));
        PartnerTradeProofDto absentProof = PartnerTradeProofDto.from(null);
        return partners.stream().map(partner -> CompanyPartnerDto.from(partner,
                proofs.getOrDefault(partner.id, absentProof))).toList();
    }

    public CompanyPartnerDto get(Long id) {
        return CompanyPartnerDto.from(findOrThrow(id));
    }

    @Transactional
    public CompanyPartnerDto create(PartnerRequest request) {
        String partnerNumber = PartnerNumber.canonicalize(request.partnerNumber());
        assertPartnerNumberFree(partnerNumber, null);
        Partner partner = new Partner();
        partner.partnerNumber = partnerNumber;
        applyCompanyFields(partner, request);
        partner.persist();
        return CompanyPartnerDto.from(partner);
    }

    @Transactional
    public CompanyPartnerDto update(Long id, PartnerRequest request) {
        Partner partner = findOrThrow(id);
        String partnerNumber = PartnerNumber.canonicalize(request.partnerNumber());
        assertPartnerNumberFree(partnerNumber, id);
        partner.partnerNumber = partnerNumber;
        applyCompanyFields(partner, request);
        return CompanyPartnerDto.from(partner);
    }

    @Transactional
    public PartnerDto updateOwnContactData(Partner partner, PartnerContactRequest request) {
        partner.name = request.name();
        partner.address = request.address().toEntity();
        partner.email = request.email();
        partner.phone = request.phone();
        return PartnerDto.from(partner);
    }

    // Case-insensitive to match the importer's duplicate check (PartnerImportDefinition),
    // so "P-1001" and "p-1001" can't coexist depending on which path created the partner.
    // Locale.ROOT to match PartnerNumber.canonicalize's case-folding (avoids the Turkish-I trap).
    private void assertPartnerNumberFree(String partnerNumber, Long excludingId) {
        Partner existing = Partner.find("lower(partnerNumber) = ?1", partnerNumber.toLowerCase(Locale.ROOT))
                .firstResult();
        if (existing != null && !existing.id.equals(excludingId)) {
            throw new ApiException(409, "Partnernummer bereits vergeben.");
        }
    }

    private void applyCompanyFields(Partner partner, PartnerRequest request) {
        partner.name = request.name();
        partner.address = request.address().toEntity();
        partner.email = request.email();
        partner.phone = request.phone();
        partner.commissionPercent = request.commissionPercent();
        partner.taxIdentifier = taxIdentifierValidator.normalize(request.taxIdentifier());
        // Keep older full-update clients from deleting a note they do not know about.
        // The current company form sends an empty string explicitly when the user clears it.
        if (request.internalNoteProvided()) {
            partner.internalNote = request.internalNote() == null || request.internalNote().isBlank()
                    ? null : request.internalNote();
        }
        partner.active = request.active() == null || request.active();
    }

    public Partner findOrThrow(Long id) {
        Partner partner = Partner.findById(id);
        if (partner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        return partner;
    }
}
