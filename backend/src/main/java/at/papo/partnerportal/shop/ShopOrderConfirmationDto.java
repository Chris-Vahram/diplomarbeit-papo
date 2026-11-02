package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.ArticleTax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// What the shop shows and prints after a checkout (shop-checkout "Order confirmation in the shop").
// Built from the stored order, so a repeated submit returns exactly the same receipt. No commission
// or internal partner data.
public record ShopOrderConfirmationDto(
        Long orderNumber,
        LocalDate orderDate,
        List<Line> items,
        BigDecimal grossTotal,
        BigDecimal netTotal,
        BigDecimal taxTotal,
        List<TaxLine> taxes,
        Customer customer,
        // Seller in the legal sense is the company; the partner owning the shop is the contact.
        Seller seller,
        Contact contact,
        // QUEUED/REDIRECTED: a confirmation mail is on its way; anything else: no mail is sent.
        String customerEmailNotice
) {
    public record Line(String name, int quantity, BigDecimal unitGross, BigDecimal lineGross, BigDecimal taxRatePercent) {
    }

    public record TaxLine(BigDecimal ratePercent, BigDecimal net, BigDecimal tax) {
    }

    public record Customer(String firstName, String lastName, String street, String houseNumber, String postalCode,
                           String city, String country, String email, String phone) {
    }

    public record Seller(String name, String taxIdentifier) {
    }

    public record Contact(String name, String street, String houseNumber, String postalCode, String city,
                          String country, String email, String phone) {
    }

    static ShopOrderConfirmationDto from(Order order, String customerEmailNotice) {
        List<Line> lines = order.items.stream()
                .map(item -> new Line(item.product.name, item.quantity, item.unitPriceSnapshot,
                        item.unitPriceSnapshot.multiply(BigDecimal.valueOf(item.quantity)), item.taxRatePercentSnapshot))
                .toList();
        Map<BigDecimal, List<OrderItem>> byRate = order.items.stream()
                .collect(Collectors.groupingBy(item -> ArticleTax.normalized(item.taxRatePercentSnapshot),
                        TreeMap::new, Collectors.toList()));
        List<TaxLine> taxes = byRate.entrySet().stream()
                .map(entry -> {
                    ArticleTax.Totals totals = ArticleTax.totals(entry.getValue().stream()
                            .map(item -> new ArticleTax.Line(item.unitPriceSnapshot, item.quantity, item.taxRatePercentSnapshot))
                            .toList());
                    return new TaxLine(entry.getKey(), totals.net(), totals.tax());
                })
                .toList();
        var kunde = order.kunde;
        Customer customer = new Customer(kunde.firstName, kunde.lastName, kunde.address.street, kunde.address.houseNumber,
                kunde.address.postalCode, kunde.address.city, kunde.address.country, kunde.email, kunde.phone);
        return new ShopOrderConfirmationDto(order.id, order.orderDate, lines, order.orderValue, order.netValue,
                order.taxValue, taxes, customer, seller(order.partner.company), contact(order.partner),
                customerEmailNotice);
    }

    private static Seller seller(CompanyProfile company) {
        if (company == null) {
            return null;
        }
        return new Seller(company.businessName, company.taxIdentifier == null ? null : company.taxIdentifier.value);
    }

    private static Contact contact(Partner partner) {
        var address = partner.address;
        return new Contact(partner.name,
                address == null ? null : address.street, address == null ? null : address.houseNumber,
                address == null ? null : address.postalCode, address == null ? null : address.city,
                address == null ? null : address.country, partner.email, partner.phone);
    }
}
