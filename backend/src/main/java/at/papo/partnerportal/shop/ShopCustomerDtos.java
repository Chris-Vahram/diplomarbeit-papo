package at.papo.partnerportal.shop;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.order.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// Request and response shapes of the customer account API (shop-customer-accounts spec).
// Nothing here carries commission, partner number or other internal data.
public final class ShopCustomerDtos {

    private ShopCustomerDtos() {
    }

    // loginEmail is the verified address from the token and cannot be changed here.
    public record Profile(String loginEmail, String lastName, String firstName, String street, String houseNumber,
                          String postalCode, String city, String country, String email, String phone) {

        static Profile of(String loginEmail, ShopCustomerAccount account) {
            if (account == null) {
                return new Profile(loginEmail, null, null, null, null, null, null, null, loginEmail, null);
            }
            return new Profile(loginEmail, account.lastName, account.firstName, account.street, account.houseNumber,
                    account.postalCode, account.city, account.country, account.email, account.phone);
        }
    }

    public record ProfileRequest(String lastName, String firstName, String street, String houseNumber,
                                 String postalCode, String city, String country, String email, String phone) {
    }

    public record OrderLine(String name, int quantity, BigDecimal unitGross, BigDecimal lineGross) {
    }

    public record Address(String firstName, String lastName, String street, String houseNumber, String postalCode,
                          String city, String country, String email, String phone) {

        static Address of(Kunde kunde) {
            var address = kunde.address;
            return new Address(kunde.firstName, kunde.lastName,
                    address == null ? null : address.street, address == null ? null : address.houseNumber,
                    address == null ? null : address.postalCode, address == null ? null : address.city,
                    address == null ? null : address.country, kunde.email, kunde.phone);
        }
    }

    // status: SUBMITTED | REVIEWED | FULFILLED, labelled by the shop.
    public record CustomerOrder(Long orderNumber, LocalDate orderDate, String status, BigDecimal grossTotal,
                                List<OrderLine> items, Address delivery, String shopName) {

        static CustomerOrder of(Order order) {
            List<OrderLine> lines = order.items.stream()
                    .map(item -> new OrderLine(item.product.name, item.quantity, item.unitPriceSnapshot,
                            item.unitPriceSnapshot.multiply(BigDecimal.valueOf(item.quantity))))
                    .toList();
            return new CustomerOrder(order.id, order.orderDate, order.status.name(), order.orderValue, lines,
                    Address.of(order.kunde), order.partner.name);
        }
    }

    // Download of everything stored for the account ("Data export and account deletion").
    public record Export(Instant exportedAt, Profile profile, Instant accountCreatedAt, List<CustomerOrder> orders) {
    }
}
