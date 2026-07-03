package at.papo.partnerportal.product;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProductService {

    public List<ProductDto> list() {
        return Product.<Product>listAll().stream().map(ProductDto::from).toList();
    }

    public ProductDto get(Long id) {
        return ProductDto.from(findOrThrow(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        product.persist();
        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        applyRequest(product, request);
        return ProductDto.from(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.name = request.name();
        product.price = request.price();
        product.description = request.description();
        product.available = request.available() == null || request.available();
        product.category = request.category();
        product.imageUrl = request.imageUrl();
    }

    Product findOrThrow(Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new ApiException(404, "Produkt nicht gefunden.");
        }
        return product;
    }
}
