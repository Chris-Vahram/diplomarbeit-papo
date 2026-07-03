package at.papo.partnerportal.enablement;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class EnablementService {

    public List<EnablementDto> listForPartner(Long partnerId) {
        Partner partner = findPartnerOrThrow(partnerId);
        Set<Long> enabledProductIds = ProductEnablement.<ProductEnablement>list("partner", partner).stream()
                .map(e -> e.product.id)
                .collect(java.util.stream.Collectors.toSet());
        return Product.<Product>listAll().stream()
                .map(product -> EnablementDto.from(product, enabledProductIds.contains(product.id)))
                .toList();
    }

    @Transactional
    public void enable(Long partnerId, Long productId) {
        Partner partner = findPartnerOrThrow(partnerId);
        Product product = findProductOrThrow(productId);
        boolean exists = ProductEnablement.count("partner = ?1 and product = ?2", partner, product) > 0;
        if (!exists) {
            ProductEnablement enablement = new ProductEnablement();
            enablement.partner = partner;
            enablement.product = product;
            enablement.persist();
        }
    }

    @Transactional
    public void disable(Long partnerId, Long productId) {
        Partner partner = findPartnerOrThrow(partnerId);
        Product product = findProductOrThrow(productId);
        ProductEnablement.delete("partner = ?1 and product = ?2", partner, product);
    }

    public List<ProductDto> listEnabledProducts(Partner partner) {
        return ProductEnablement.<ProductEnablement>list("partner", partner).stream()
                .map(e -> ProductDto.from(e.product))
                .toList();
    }

    private Partner findPartnerOrThrow(Long partnerId) {
        Partner partner = Partner.findById(partnerId);
        if (partner == null) {
            throw new ApiException(404, "Partner nicht gefunden.");
        }
        return partner;
    }

    private Product findProductOrThrow(Long productId) {
        Product product = Product.findById(productId);
        if (product == null) {
            throw new ApiException(404, "Produkt nicht gefunden.");
        }
        return product;
    }
}
