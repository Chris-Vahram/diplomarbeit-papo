package at.papo.partnerportal.order;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeService;
import at.papo.partnerportal.enablement.EnablementService;
import at.papo.partnerportal.mail.OrderEmailService;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.partner.PartnerService;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.security.CurrentCompany;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderService {

    // Forward-only: submitted -> reviewed -> fulfilled, no skips or reverts (order-management
    // spec). Dashboard/settlement totals count fulfilled orders only, so an allowed revert
    // would retroactively shift already-reported numbers.
    private static final Map<OrderStatus, OrderStatus> NEXT_STATUS = Map.of(
            OrderStatus.SUBMITTED, OrderStatus.REVIEWED,
            OrderStatus.REVIEWED, OrderStatus.FULFILLED
    );

    @Inject
    KundeService kundeService;

    @Inject
    EnablementService enablementService;

    @Inject
    CommissionService commissionService;

    @Inject
    OrderEmailService orderEmailService;

    @Inject
    PartnerService partnerService;

    @Inject
    CurrentCompany currentCompany;

    // join fetch + distinct: one query instead of 1+N+N+N (items/partner/kunde each
    // loaded per order without it). distinct collapses the duplicate parent rows the
    // items join produces (one row per item) back to one Order per id.
    public List<OrderDto> listForPartner(Partner partner) {
        List<Order> orders = Order.find(
                "select distinct o from Order o join fetch o.items join fetch o.partner join fetch o.kunde "
                        + "where o.partner = ?1 order by o.id desc",
                partner).list();
        Map<Long, CommissionRecord> commissionsByOrderId = commissionService.findForOrders(orders);
        return orders.stream()
                .map(order -> OrderDto.from(order, commissionsByOrderId.get(order.id)))
                .toList();
    }

    public List<OrderDto> listAll() {
        List<Order> orders = Order.find(
                "select distinct o from Order o join fetch o.items join fetch o.partner join fetch o.kunde "
                        + "where o.partner.company = ?1 order by o.id", currentCompany.resolve()).list();
        Map<Long, CommissionRecord> commissionsByOrderId = commissionService.findForOrders(orders);
        return orders.stream()
                .map(order -> OrderDto.from(order, commissionsByOrderId.get(order.id)))
                .toList();
    }

    public long countUnseen() {
        return Order.count("seenByCompany = false and partner.company = ?1", currentCompany.resolve());
    }

    // Manual order creation by the company (order-management spec): resolves the
    // company-selected partner, then reuses createForPartner unchanged so validation
    // (enablement, availability, commission snapshot) is identical to the partner's own flow.
    @Transactional
    public OrderDto createForPartnerId(Long partnerId, CompanyOrderRequest request) {
        Partner partner = partnerService.findOrThrow(partnerId);
        // FR-23: an inactive partner may not receive new orders, including manual
        // orders created by the company on the partner's behalf.
        if (!partner.active) {
            throw new ApiException(409, "Partner ist inaktiv und kann keine neuen Bestellungen erhalten.");
        }
        return createForPartner(partner, request.toOrderRequest());
    }

    @Transactional
    public OrderDto createForPartner(Partner partner, OrderRequest request) {
        Order order = new Order();
        order.partner = partner;
        order.kunde = resolveKunde(partner, request);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = Product.findById(itemRequest.productId());
            // An article of another company is reported like a missing one.
            if (product == null || !product.company.id.equals(partner.company.id)) {
                throw new ApiException(404, "Artikel nicht gefunden.");
            }
            if (!enablementService.isEnabledForPartner(partner, product.id)) {
                throw new ApiException(400, "Artikel \"" + product.name + "\" ist für Sie nicht freigeschaltet.");
            }
            if (!product.available) {
                throw new ApiException(400, "Artikel \"" + product.name + "\" ist derzeit nicht verfügbar.");
            }

            OrderItem item = new OrderItem();
            item.order = order;
            item.product = product;
            item.quantity = itemRequest.quantity();
            item.unitPriceSnapshot = product.price;
            order.items.add(item);
            total = total.add(product.price.multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.orderValue = total;
        order.persist();

        // Partner.commissionPercent is @NotNull/non-nullable end to end (PartnerRequest,
        // Partner entity) - there is no reachable "missing commissionPercent" state to
        // guard against here (commission spec's "missing" scenario is satisfied by
        // construction, not by a runtime check).
        CommissionRecord commission = commissionService.createForOrder(order, partner);
        // Outbox rows in the same transaction: a rolled-back order never mails (order-emails spec).
        String notice = orderEmailService.queueForNewOrder(order);
        return OrderDto.from(order, commission).withNotice(notice);
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, OrderStatus status) {
        Order order = findOwnOrThrow(orderId);
        OrderStatus current = order.status;
        if (!status.equals(NEXT_STATUS.get(current))) {
            throw new ApiException(400, "Status kann nur schrittweise vorwärts geändert werden "
                    + "(Eingereicht → Geprüft → Abgeschlossen).");
        }
        // Conditional update instead of a full-row flush: of two concurrent transitions only one
        // wins, and a concurrent markSeen cannot write back a stale status (send-order-emails design).
        if (Order.update("status = ?1 where id = ?2 and status = ?3", status, orderId, current) == 0) {
            throw new ApiException(409, "Die Bestellung wurde inzwischen geändert. Bitte die Liste neu laden.");
        }
        Order.getEntityManager().refresh(order);
        String notice = status == OrderStatus.FULFILLED ? orderEmailService.queueShipping(order) : null;
        return OrderDto.from(order, commissionService.findForOrder(order)).withNotice(notice);
    }

    // Flips the unseen flag when the company opens an order's details overlay
    // (order-notifications spec: "opening details" is the seen trigger).
    @Transactional
    public OrderDto markSeen(Long orderId) {
        Order order = findOwnOrThrow(orderId);
        // Targeted update of the flag only, so it never overwrites a concurrent status change.
        Order.update("seenByCompany = true where id = ?1", orderId);
        Order.getEntityManager().refresh(order);
        return OrderDto.from(order, commissionService.findForOrder(order));
    }

    // Company-side lookup: an order of another company is indistinguishable from a missing one.
    public Order findOwnOrThrow(Long orderId) {
        Order order = Order.find("id = ?1 and partner.company = ?2", orderId, currentCompany.resolve()).firstResult();
        if (order == null) {
            throw new ApiException(404, "Bestellung nicht gefunden.");
        }
        return order;
    }

    private Kunde resolveKunde(Partner partner, OrderRequest request) {
        if (request.kundeId() != null) {
            return kundeService.findOwnedOrThrow(partner, request.kundeId());
        }
        if (request.newKunde() != null) {
            return kundeService.createEntity(partner, request.newKunde());
        }
        throw new ApiException(400, "Bestehenden Kunden auswählen oder neue Kundendaten angeben.");
    }
}
