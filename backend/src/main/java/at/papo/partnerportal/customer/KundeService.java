package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

// Kunde records are scoped per creating partner (design.md, "Kunde scoping").
@ApplicationScoped
public class KundeService {

    public List<KundeDto> listForPartner(Partner partner) {
        return Kunde.<Kunde>list("ownerPartner", partner).stream().map(KundeDto::from).toList();
    }

    @Transactional
    public KundeDto create(Partner partner, KundeRequest request) {
        return KundeDto.from(createEntity(partner, request));
    }

    @Transactional
    public Kunde createEntity(Partner partner, KundeRequest request) {
        Kunde kunde = new Kunde();
        kunde.ownerPartner = partner;
        kunde.lastName = request.lastName();
        kunde.address = request.address();
        kunde.firstName = request.firstName();
        kunde.email = request.email();
        kunde.phone = request.phone();
        kunde.persist();
        return kunde;
    }

    public Kunde findOwnedOrThrow(Partner partner, Long id) {
        Kunde kunde = Kunde.find("id = ?1 and ownerPartner = ?2", id, partner).firstResult();
        if (kunde == null) {
            throw new ApiException(404, "Kunde nicht gefunden.");
        }
        return kunde;
    }
}
