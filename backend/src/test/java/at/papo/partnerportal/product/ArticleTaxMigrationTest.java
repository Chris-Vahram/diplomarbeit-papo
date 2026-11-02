package at.papo.partnerportal.product;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

// add-article-tax-rate D5: pre-change rows (no rate, no net/tax) get 20 % and derived net/tax;
// prices and historic commission records stay unchanged; a second run changes nothing.
@QuarkusTest
class ArticleTaxMigrationTest {

    @Inject
    ArticleTaxMigrationService migrationService;

    @Test
    void backfillsRatesAndOrderNetWithoutTouchingPricesOrCommissions() {
        Long[] ids = QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = Partner.find("partnerNumber", "PARTNER-DEMO").firstResult();
            Product product = Product.find("name", "Akkuschrauber 18V Set").firstResult();
            Kunde kunde = new Kunde();
            kunde.ownerPartner = partner;
            kunde.lastName = "Migrationstest";
            kunde.address = new Address("Teststr.", "1", "4020", "Linz", "Österreich");
            kunde.persist();
            Order order = new Order();
            order.partner = partner;
            order.kunde = kunde;
            order.orderValue = new BigDecimal("258.00");
            OrderItem item = new OrderItem();
            item.order = order;
            item.product = product;
            item.quantity = 2;
            item.unitPriceSnapshot = product.price;
            order.items.add(item);
            order.persist();
            // Pre-change commission: gross basis.
            CommissionRecord record = new CommissionRecord();
            record.order = order;
            record.partner = partner;
            record.orderValue = new BigDecimal("258.00");
            record.commissionPercentUsed = new BigDecimal("10.00");
            record.commissionValue = new BigDecimal("25.80");
            record.persist();
            Product.update("taxRatePercent = null where id = ?1", product.id);
            return new Long[] { order.id, item.id, product.id };
        });

        migrationService.migrate();
        migrationService.migrate();

        QuarkusTransaction.requiringNew().run(() -> {
            Product product = Product.findById(ids[2]);
            assertEquals(new BigDecimal("20.00"), product.taxRatePercent);
            assertEquals(new BigDecimal("129.00"), product.price);
            OrderItem item = OrderItem.findById(ids[1]);
            assertEquals(new BigDecimal("20.00"), item.taxRatePercentSnapshot);
            Order order = Order.findById(ids[0]);
            assertEquals(new BigDecimal("258.00"), order.orderValue);
            assertEquals(new BigDecimal("215.00"), order.netValue);
            assertEquals(new BigDecimal("43.00"), order.taxValue);
            CommissionRecord record = CommissionRecord.find("order", order).firstResult();
            assertEquals(new BigDecimal("258.00"), record.orderValue);
            assertEquals(new BigDecimal("25.80"), record.commissionValue);
            Kunde kunde = order.kunde;
            record.delete();
            order.delete();
            kunde.delete();
        });
    }
}
