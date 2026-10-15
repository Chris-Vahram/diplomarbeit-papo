package at.papo.partnerportal.mail;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import io.quarkus.mailer.Mail;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// German plain-text bodies. Customer mails carry no prices (net/gross for end customers is
// still an open company question, send-order-emails design "Content").
final class OrderEmailContent {

    private static final Locale DE_AT = Locale.forLanguageTag("de-AT");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private OrderEmailContent() {
    }

    static Mail build(OrderEmail email, String to, boolean redirected) {
        Order order = email.order;
        String sender = senderName();
        String subject;
        StringBuilder body = new StringBuilder();
        if (redirected) {
            body.append("[Testumleitung – ursprünglich an: ").append(roleLabel(email.recipientRole)).append("]\n\n");
        }
        switch (email.type) {
            case ORDER_CONFIRMATION -> {
                subject = "Ihre Bestellung Nr. " + order.id + " ist eingegangen";
                body.append("Guten Tag ").append(customerName(order.kunde)).append(",\n\n")
                        .append("vielen Dank für Ihre Bestellung bei ").append(order.partner.name)
                        .append(". Folgende Artikel haben wir erhalten:\n\n");
                appendItems(body, order, false);
                appendDelivery(body, order);
                body.append("\nBei Fragen wenden Sie sich bitte an ").append(order.partner.name).append(".\n");
            }
            case SHIPPING_CONFIRMATION -> {
                subject = "Ihre Bestellung Nr. " + order.id + " wurde abgeschlossen";
                body.append("Guten Tag ").append(customerName(order.kunde)).append(",\n\n")
                        .append("Ihre Bestellung Nr. ").append(order.id).append(" vom ")
                        .append(order.orderDate.format(DATE)).append(" wurde abgeschlossen:\n\n");
                appendItems(body, order, false);
                appendDelivery(body, order);
                body.append("\nBei Fragen wenden Sie sich bitte an ").append(order.partner.name).append(".\n");
            }
            case ORDER_NOTICE_PARTNER -> {
                subject = "Neue Bestellung Nr. " + order.id + " für " + customerName(order.kunde);
                body.append("Guten Tag,\n\nfür Ihre Partnerfirma ").append(order.partner.name)
                        .append(" wurde eine neue Bestellung erfasst.\n\n");
                appendBusiness(body, order);
            }
            case ORDER_NOTICE_COMPANY -> {
                subject = "Neue Bestellung Nr. " + order.id + " von " + order.partner.name;
                body.append("Guten Tag,\n\nim Partnerportal ist eine neue Bestellung eingegangen.\n\n")
                        .append("Partner: ").append(order.partner.name).append(" (").append(order.partner.partnerNumber).append(")\n");
                appendBusiness(body, order);
            }
            default -> throw new IllegalStateException("Unbekannter E-Mail-Typ");
        }
        body.append("\nFreundliche Grüße\n").append(sender).append("\n");
        return Mail.withText(to, subject, body.toString());
    }

    private static void appendBusiness(StringBuilder body, Order order) {
        NumberFormat eur = NumberFormat.getCurrencyInstance(DE_AT);
        body.append("Bestellnummer: ").append(order.id).append(", Datum: ").append(order.orderDate.format(DATE)).append("\n")
                .append("Endkunde: ").append(customerName(order.kunde)).append("\n\n");
        appendItems(body, order, true);
        body.append("\nBestellwert: ").append(eur.format(order.orderValue)).append("\n");
        CommissionRecord commission = CommissionRecord.find("order", order).firstResult();
        if (commission != null) {
            body.append("Provision: ").append(eur.format(commission.commissionValue))
                    .append(" (").append(commission.commissionPercentUsed.stripTrailingZeros().toPlainString()).append(" %)\n");
        }
    }

    private static void appendItems(StringBuilder body, Order order, boolean withPrices) {
        NumberFormat eur = NumberFormat.getCurrencyInstance(DE_AT);
        for (OrderItem item : order.items) {
            body.append("- ").append(item.quantity).append(" × ").append(item.product.name);
            if (withPrices) {
                body.append(" à ").append(eur.format(item.unitPriceSnapshot))
                        .append(" = ").append(eur.format(item.unitPriceSnapshot.multiply(BigDecimal.valueOf(item.quantity))));
            }
            body.append("\n");
        }
    }

    private static void appendDelivery(StringBuilder body, Order order) {
        Address a = order.kunde.address;
        if (a == null) {
            return;
        }
        body.append("\nLieferadresse:\n").append(customerName(order.kunde)).append("\n")
                .append(a.street).append(" ").append(a.houseNumber).append("\n")
                .append(a.postalCode).append(" ").append(a.city).append("\n")
                .append(a.country).append("\n");
    }

    private static String customerName(Kunde kunde) {
        return (kunde.firstName != null && !kunde.firstName.isBlank() ? kunde.firstName + " " : "") + kunde.lastName;
    }

    private static String senderName() {
        CompanyProfile profile = CompanyProfile.find("singletonKey", CompanyProfile.CURRENT_KEY).firstResult();
        return profile != null && profile.businessName != null ? profile.businessName : "PAPO Partnerportal";
    }

    private static String roleLabel(OrderEmail.RecipientRole role) {
        return switch (role) {
            case CUSTOMER -> "Endkunde";
            case PARTNER -> "Partner";
            case COMPANY -> "Firma";
        };
    }
}
