package at.papo.partnerportal.order;

import at.papo.partnerportal.customer.KundeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Manual order creation by the company on a partner's behalf (order-management spec).
// Same kundeId/newKunde exactly-one rule as OrderRequest, plus an explicit partnerId
// since the company (unlike a partner) isn't scoped to a single partner already.
public record CompanyOrderRequest(
        @NotNull(message = "Partner ist erforderlich.") Long partnerId,
        Long kundeId,
        @Valid KundeRequest newKunde,
        @NotEmpty(message = "Bestellung muss mindestens einen Artikel enthalten.")
        List<@Valid OrderItemRequest> items
) {
    public OrderRequest toOrderRequest() {
        return new OrderRequest(kundeId, newKunde, items);
    }
}
