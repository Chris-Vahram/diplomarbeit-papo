package at.papo.partnerportal.shop;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// Shops created before add-shop-page-renderer get their theme and pages on the next start
// (design.md Migration Plan). Idempotent, so every start may run it.
@ApplicationScoped
public class ShopPageBootstrap {

    @Inject
    ShopPageService pageService;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        PartnerShop.<PartnerShop>listAll().forEach(pageService::ensureDefaults);
    }
}
