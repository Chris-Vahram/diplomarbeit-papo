package at.papo.partnerportal.product;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ProductService {

    // Matches the internal serve reference a product's imageUrl gets set to when the company
    // uploads/picks an image ("/product-images/{id}", see ProductImageDto.from for why this
    // deliberately omits the "/api" prefix) - distinguishes an internal image reference from
    // a pasted external URL.
    private static final Pattern INTERNAL_IMAGE_URL = Pattern.compile("^/product-images/(\\d+)$");

    @Inject
    ProductImageService productImageService;

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
        applyImage(product, request.imageUrl());
    }

    private void applyImage(Product product, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            product.image = null;
            product.imageUrl = null;
            return;
        }
        Matcher matcher = INTERNAL_IMAGE_URL.matcher(imageUrl);
        if (matcher.matches()) {
            ProductImage image = productImageService.findOrThrow(Long.valueOf(matcher.group(1)));
            product.image = image;
            product.imageUrl = "/product-images/" + image.id;
        } else {
            product.image = null;
            product.imageUrl = imageUrl;
        }
    }

    Product findOrThrow(Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new ApiException(404, "Produkt nicht gefunden.");
        }
        return product;
    }
}
