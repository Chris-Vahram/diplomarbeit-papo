package at.papo.partnerportal.order;

import at.papo.partnerportal.customer.KundeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Exactly one of kundeId (existing, must belong to the calling partner) or
// newKunde (created on the fly) is required - enforced in OrderService, not
// here, since it's a cross-field business rule rather than a single-field one.
public record OrderRequest(
        Long kundeId,
        @Valid KundeRequest newKunde,
        @NotEmpty(message = "Bestellung muss mindestens einen Artikel enthalten.")
        List<@Valid OrderItemRequest> items
) {
}
