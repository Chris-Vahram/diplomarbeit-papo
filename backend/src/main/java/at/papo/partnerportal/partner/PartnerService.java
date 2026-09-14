package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class PartnerService {

    public List<CompanyPartnerDto> list() {
        return Partner.<Partner>listAll().stream().map(CompanyPartnerDto::from).toList();
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
        partner.internalNote = request.internalNote() == null || request.internalNote().isBlank()
                ? null : request.internalNote();
        partner.active = request.active() == null || request.active();
    }

    Partner findOrThrow(Long id) {
        Partner partner = Partner.findById(id);
        if (partner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        return partner;
    }
}
