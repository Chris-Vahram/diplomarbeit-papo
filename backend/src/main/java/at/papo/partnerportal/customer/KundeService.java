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
        return createEntity(partner, request, KundeOrigin.PORTAL);
    }

    // Shop orders always create a new record (customer-management "Customer origin"): no
    // matching against existing customers, so a visitor can never change partner data.
    @Transactional
    public Kunde createEntity(Partner partner, KundeRequest request, KundeOrigin origin) {
        Kunde kunde = new Kunde();
        kunde.ownerPartner = partner;
        kunde.origin = origin;
        kunde.lastName = request.lastName();
        kunde.address = request.address().toEntity();
        kunde.firstName = request.firstName();
        kunde.email = request.email();
        kunde.phone = request.phone();
        kunde.persist();
        return kunde;
    }

    // Edits the shared record in place, so existing orders show the new data too
    // (no snapshot - edit-partner-end-customers design).
    @Transactional
    public KundeDto update(Partner partner, Long id, KundeRequest request) {
        Kunde kunde = findOwnedOrThrow(partner, id);
        kunde.lastName = request.lastName();
        kunde.address = request.address().toEntity();
        kunde.firstName = request.firstName();
        kunde.email = request.email();
        kunde.phone = request.phone();
        LocalDate lastOrderDate = Kunde.getEntityManager()
                .createQuery("select max(o.orderDate) from Order o where o.kunde = :kunde", LocalDate.class)
                .setParameter("kunde", kunde)
                .getSingleResult();
        return KundeDto.from(kunde, lastOrderDate);
    }

    public Kunde findOwnedOrThrow(Partner partner, Long id) {
        Kunde kunde = Kunde.find("id = ?1 and ownerPartner = ?2", id, partner).firstResult();
        if (kunde == null) {
            throw new ApiException(404, "Kunde nicht gefunden.");
        }
        return kunde;
    }
}
