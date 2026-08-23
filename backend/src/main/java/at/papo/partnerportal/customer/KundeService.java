package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

// Kunde records are scoped per creating partner (design.md, "Kunde scoping").
@ApplicationScoped
public class KundeService {

    // Single grouped left-join query — no per-Kunde order lookups (design.md, decision 2).
    private static final String LIST_WITH_LAST_ORDER_DATE = """
            select k, max(o.orderDate)
            from Kunde k left join Order o on o.kunde = k
            where k.ownerPartner = :partner
            group by k
            """;

    public List<KundeDto> listForPartner(Partner partner) {
        List<Object[]> rows = Kunde.getEntityManager()
                .createQuery(LIST_WITH_LAST_ORDER_DATE, Object[].class)
                .setParameter("partner", partner)
                .getResultList();
        return rows.stream()
                .map(row -> KundeDto.from((Kunde) row[0], (LocalDate) row[1]))
                .toList();
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
        kunde.address = request.address().toEntity();
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
