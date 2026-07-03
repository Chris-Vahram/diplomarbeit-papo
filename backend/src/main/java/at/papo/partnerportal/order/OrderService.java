package at.papo.partnerportal.order;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeService;
import at.papo.partnerportal.enablement.EnablementService;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class OrderService {

    @Inject
    KundeService kundeService;

    @Inject
    EnablementService enablementService;

    public List<OrderDto> listForPartner(Partner partner) {
        return Order.<Order>list("partner", partner).stream().map(OrderDto::from).toList();
    }

    public List<OrderDto> listAll() {
        return Order.<Order>listAll().stream().map(OrderDto::from).toList();
    }

    @Transactional
    public OrderDto createForPartner(Partner partner, OrderRequest request) {
        Order order = new Order();
        order.partner = partner;
        order.kunde = resolveKunde(partner, request);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = Product.findById(itemRequest.productId());
            if (product == null) {
                throw new ApiException(404, "Produkt nicht gefunden.");
            }
            if (!enablementService.isEnabledForPartner(partner, product.id)) {
                throw new ApiException(400, "Produkt \"" + product.name + "\" ist für Sie nicht freigeschaltet.");
            }
            if (!product.available) {
                throw new ApiException(400, "Produkt \"" + product.name + "\" ist derzeit nicht verfügbar.");
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
        return OrderDto.from(order);
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, OrderStatus status) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new ApiException(404, "Bestellung nicht gefunden.");
        }
        order.status = status;
        return OrderDto.from(order);
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
