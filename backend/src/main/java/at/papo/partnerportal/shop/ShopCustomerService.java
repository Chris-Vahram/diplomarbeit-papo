package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderOrigin;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

// Profile, own orders, export and deletion of an end-customer account (shop-customer-accounts
// spec). Every method takes the verified token subject; nothing is looked up by a client id.
@ApplicationScoped
public class ShopCustomerService {

    static final String ORDER_NOT_FOUND = "Diese Bestellung gibt es nicht.";
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()/\\-. ]{4,}$");

    @Inject
    ShopAccountDirectory directory;

    public ShopCustomerDtos.Profile profile(String subject, String loginEmail) {
        return ShopCustomerDtos.Profile.of(loginEmail, find(subject));
    }

    @Transactional
    public ShopCustomerDtos.Profile updateProfile(String subject, String loginEmail, ShopCustomerDtos.ProfileRequest request) {
        if (request == null) {
            throw new ApiException(400, "Bitte die Angaben prüfen.");
        }
        String email = clean(request.email());
        String phone = clean(request.phone());
        String[] texts = {request.lastName(), request.firstName(), request.street(), request.houseNumber(),
                request.postalCode(), request.city(), request.country()};
        for (String value : texts) {
            String cleaned = clean(value);
            if (cleaned != null && cleaned.length() > ShopOrderService.TEXT_MAX) {
                throw new ApiException(400, "Eine Angabe ist zu lang (maximal " + ShopOrderService.TEXT_MAX + " Zeichen).");
            }
        }
        if (email != null && (email.length() > ShopOrderService.EMAIL_MAX || !EMAIL.matcher(email).matches())) {
            throw new ApiException(400, "Die E-Mail-Adresse ist ungültig.");
        }
        if (phone != null && (phone.length() > ShopOrderService.PHONE_MAX || !PHONE.matcher(phone).matches())) {
            throw new ApiException(400, "Die Telefonnummer ist ungültig.");
        }
        ShopCustomerAccount account = find(subject);
        Instant now = Instant.now();
        if (account == null) {
            account = new ShopCustomerAccount();
            account.subject = subject;
            account.createdAt = now;
        }
        account.lastName = clean(request.lastName());
        account.firstName = clean(request.firstName());
        account.street = clean(request.street());
        account.houseNumber = clean(request.houseNumber());
        account.postalCode = clean(request.postalCode());
        account.city = clean(request.city());
        account.country = clean(request.country());
        account.email = email;
        account.phone = phone;
        account.updatedAt = now;
        account.persist();
        return ShopCustomerDtos.Profile.of(loginEmail, account);
    }

    // Only orders placed with this account in this shop; guest orders never appear here.
    @Transactional
    public List<ShopCustomerDtos.CustomerOrder> orders(String subject, PartnerShop shop) {
        return Order.<Order>list("kunde.accountSubject = ?1 and partner = ?2 and origin = ?3 order by orderDate desc, id desc",
                        subject, shop.partner, OrderOrigin.SHOP)
                .stream().map(ShopCustomerDtos.CustomerOrder::of).toList();
    }

    // Same 404 for an unknown id and for another customer's order.
    @Transactional
    public ShopCustomerDtos.CustomerOrder order(String subject, PartnerShop shop, long orderId) {
        return Order.<Order>find("id = ?1 and kunde.accountSubject = ?2 and partner = ?3 and origin = ?4",
                        orderId, subject, shop.partner, OrderOrigin.SHOP)
                .firstResultOptional()
                .map(ShopCustomerDtos.CustomerOrder::of)
                .orElseThrow(() -> new ApiException(404, ORDER_NOT_FOUND));
    }

    // All shops: the export covers everything stored for the account.
    @Transactional
    public ShopCustomerDtos.Export export(String subject, String loginEmail) {
        ShopCustomerAccount account = find(subject);
        List<ShopCustomerDtos.CustomerOrder> orders = Order.<Order>list(
                        "kunde.accountSubject = ?1 order by orderDate desc, id desc", subject)
                .stream().map(ShopCustomerDtos.CustomerOrder::of).toList();
        return new ShopCustomerDtos.Export(Instant.now(), ShopCustomerDtos.Profile.of(loginEmail, account),
                account == null ? null : account.createdAt, orders);
    }

    // Profile and link go first inside the transaction; the login is removed last, so a failing
    // Keycloak call rolls everything back and the customer can simply try again. Orders and Kunde
    // stay as the partner's and company's business records with the data entered at order time.
    @Transactional
    public void delete(String subject) {
        ShopCustomerAccount.delete("subject", subject);
        Kunde.update("accountSubject = null where accountSubject = ?1", subject);
        directory.deleteLogin(subject);
    }

    private static ShopCustomerAccount find(String subject) {
        return ShopCustomerAccount.<ShopCustomerAccount>find("subject", subject).firstResult();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
