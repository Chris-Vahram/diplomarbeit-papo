package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// %test doesn't seed demo orders/Kunden (DemoDataSeeder.onStart skips seedOrders() outside
// DEVELOPMENT so other tests keep a clean order baseline) - this test builds its own minimal
// fixtures directly, including for a second partner that has no Keycloak-linked test user
// (there is no REST endpoint to create a Kunde for a partner other than the caller, which is
// exactly the scoping behaviour under test).
@QuarkusTest
class KundeServiceTest {

    @Inject
    KundeService kundeService;

    @Inject
    EntityManagerFactory entityManagerFactory;

    private Partner partner(String number) {
        return Partner.<Partner>find("partnerNumber", number).firstResult();
    }

    private Kunde persistKunde(Partner owner, String lastName) {
        Kunde kunde = new Kunde();
        kunde.ownerPartner = owner;
        kunde.lastName = lastName;
        kunde.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
        kunde.persist();
        return kunde;
    }

    private void persistOrder(Partner partner, Kunde kunde, int daysAgo) {
        Order order = new Order();
        order.partner = partner;
        order.kunde = kunde;
        order.orderDate = LocalDate.now().minusDays(daysAgo);
        order.orderValue = BigDecimal.TEN;
        order.persist();
    }

    private KundeDto byLastName(List<KundeDto> kunden, String lastName) {
        return kunden.stream().filter(k -> k.lastName().equals(lastName)).findFirst().orElseThrow();
    }

    @Test
    void lastOrderDateIsMostRecentOrderPerKundeAndNullWithoutOrders() {
        Partner p1 = partner("P-1001");
        QuarkusTransaction.requiringNew().run(() -> {
            Kunde withOrders = persistKunde(p1, "Mit Bestellungen");
            persistOrder(p1, withOrders, 40);
            persistOrder(p1, withOrders, 5);
            persistKunde(p1, "Ohne Bestellungen");
        });

        List<KundeDto> result = kundeService.listForPartner(p1);

        assertEquals(LocalDate.now().minusDays(5), byLastName(result, "Mit Bestellungen").lastOrderDate());
        assertNull(byLastName(result, "Ohne Bestellungen").lastOrderDate());
    }

    @Test
    void kundenAreScopedToTheirOwningPartnerOnly() {
        Partner p1 = partner("P-1001");
        Partner p2 = partner("P-1002");
        QuarkusTransaction.requiringNew().run(() -> persistKunde(p1, "Partner-Eins-Kunde"));
        QuarkusTransaction.requiringNew().run(() -> persistKunde(p2, "Partner-Zwei-Kunde"));

        List<KundeDto> p1Kunden = kundeService.listForPartner(p1);
        List<KundeDto> p2Kunden = kundeService.listForPartner(p2);

        assertTrue(p1Kunden.stream().anyMatch(k -> k.lastName().equals("Partner-Eins-Kunde")));
        assertTrue(p1Kunden.stream().noneMatch(k -> k.lastName().equals("Partner-Zwei-Kunde")));
        assertTrue(p2Kunden.stream().anyMatch(k -> k.lastName().equals("Partner-Zwei-Kunde")));
        assertTrue(p2Kunden.stream().noneMatch(k -> k.lastName().equals("Partner-Eins-Kunde")));
    }

    @Test
    void listForPartnerRunsExactlyOneQuery() {
        Partner p1 = partner("P-1001");
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        kundeService.listForPartner(p1);

        assertEquals(1, statistics.getQueryExecutionCount(),
                "listForPartner() must be a single grouped query, no per-Kunde lookups");
    }
}
