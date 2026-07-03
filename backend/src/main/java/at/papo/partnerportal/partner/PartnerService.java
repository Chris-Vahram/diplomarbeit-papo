package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PartnerService {

    public List<PartnerDto> list() {
        return Partner.<Partner>listAll().stream().map(PartnerDto::from).toList();
    }

    public PartnerDto get(Long id) {
        return PartnerDto.from(findOrThrow(id));
    }

    @Transactional
    public PartnerDto create(PartnerRequest request) {
        assertPartnerNumberFree(request.partnerNumber(), null);
        Partner partner = new Partner();
        partner.partnerNumber = request.partnerNumber();
        applyCompanyFields(partner, request);
        partner.persist();
        return PartnerDto.from(partner);
    }

    @Transactional
    public PartnerDto update(Long id, PartnerRequest request) {
        Partner partner = findOrThrow(id);
        assertPartnerNumberFree(request.partnerNumber(), id);
        partner.partnerNumber = request.partnerNumber();
        applyCompanyFields(partner, request);
        return PartnerDto.from(partner);
    }

    @Transactional
    public PartnerDto updateOwnContactData(Partner partner, PartnerContactRequest request) {
        partner.name = request.name();
        partner.address = request.address();
        partner.email = request.email();
        partner.phone = request.phone();
        return PartnerDto.from(partner);
    }

    private void assertPartnerNumberFree(String partnerNumber, Long excludingId) {
        Partner existing = Partner.find("partnerNumber", partnerNumber).firstResult();
        if (existing != null && !existing.id.equals(excludingId)) {
            throw new ApiException(409, "Partnernummer bereits vergeben.");
        }
    }

    private void applyCompanyFields(Partner partner, PartnerRequest request) {
        partner.name = request.name();
        partner.address = request.address();
        partner.email = request.email();
        partner.phone = request.phone();
        partner.commissionPercent = request.commissionPercent();
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
