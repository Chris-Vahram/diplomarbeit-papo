package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.BusinessTaxIdentifierValidator;
import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.security.CurrentCompany;
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

    @Inject
    CurrentCompany currentCompany;

    public List<CompanyPartnerDto> list() {
        return list(currentCompany.resolve());
    }

    public List<CompanyPartnerDto> list(CompanyProfile company) {
        List<Partner> partners = Partner.list("company", company);
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
        CompanyProfile company = currentCompany.resolve();
        String partnerNumber = PartnerNumber.canonicalize(request.partnerNumber());
        assertPartnerNumberFree(company, partnerNumber, null);
        Partner partner = new Partner();
        partner.company = company;
        partner.partnerNumber = partnerNumber;
        applyCompanyFields(partner, request);
        partner.persist();
        return CompanyPartnerDto.from(partner);
    }

    @Transactional
    public CompanyPartnerDto update(Long id, PartnerRequest request) {
        Partner partner = findOrThrow(id);
        String partnerNumber = PartnerNumber.canonicalize(request.partnerNumber());
        assertPartnerNumberFree(partner.company, partnerNumber, id);
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
    private void assertPartnerNumberFree(CompanyProfile company, String partnerNumber, Long excludingId) {
        Partner existing = Partner.find("company = ?1 and lower(partnerNumber) = ?2", company,
                partnerNumber.toLowerCase(Locale.ROOT)).firstResult();
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

    // Company-side lookup: a partner of another company is indistinguishable from a missing one.
    public Partner findOrThrow(Long id) {
        Partner partner = Partner.find("id = ?1 and company = ?2", id, currentCompany.resolve()).firstResult();
        if (partner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        return partner;
    }
}
