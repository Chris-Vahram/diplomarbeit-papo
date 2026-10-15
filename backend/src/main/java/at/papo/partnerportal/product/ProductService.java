package at.papo.partnerportal.product;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.category.CategoryNames;
import at.papo.partnerportal.category.CategoryService;
import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
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

    @Inject
    CategoryService categoryService;

    @Inject
    EntityManager entityManager;

    public List<ProductDto> list() {
        return Product.<Product>find("select p from Product p left join fetch p.articleCategory order by p.id")
                .list().stream().map(ProductDto::from).toList();
    }

    public ProductDto get(Long id) {
        return ProductDto.from(findForReadOrThrow(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Category category = categoryService.findForProductInput(request.categoryId(), request.category());
        List<Category> lockedCategories = categoryService.lockCategories(category == null
                ? List.of() : List.of(category.id));
        category = categoryService.lockedCategory(lockedCategories, category == null ? null : category.id);
        if (category != null && !category.active) {
            throw new ApiException(400, "Artikelkategorie ist deaktiviert.");
        }
        Product product = new Product();
        applyRequest(product, request, category);
        product.persist();
        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product initial = findOrThrow(id);
        Long originalCategoryId = initial.articleCategory == null ? null : initial.articleCategory.id;
        Category requestedCategory = categoryService.findForProductInput(request.categoryId(), request.category());
        List<Long> categoryIds = new ArrayList<>();
        if (originalCategoryId != null) {
            categoryIds.add(originalCategoryId);
        }
        if (requestedCategory != null) {
            categoryIds.add(requestedCategory.id);
        }
        List<Category> lockedCategories = categoryService.lockCategories(categoryIds);
        Product product = lockProduct(id);
        Long currentCategoryId = product.articleCategory == null ? null : product.articleCategory.id;
        if (!Objects.equals(originalCategoryId, currentCategoryId)) {
            throw new ApiException(409, "Artikel wurde zwischenzeitlich geändert. Bitte erneut laden.");
        }
        Category category = requestedCategory == null ? null
                : categoryService.lockedCategory(lockedCategories, requestedCategory.id);
        if (request.categoryId() == null && request.category() != null && !request.category().isBlank()
                && !category.normalizedName.equals(CategoryNames.normalized(request.category()))) {
            throw new ApiException(400, "Artikelkategorie wurde zwischenzeitlich umbenannt.");
        }
        if (category != null && !category.active && !Objects.equals(category.id, currentCategoryId)) {
            throw new ApiException(400, "Artikelkategorie ist deaktiviert.");
        }
        applyRequest(product, request, category);
        return ProductDto.from(product);
    }

    private void applyRequest(Product product, ProductRequest request, Category category) {
        product.name = request.name();
        product.price = request.price();
        product.description = request.description();
        product.available = request.available() == null || request.available();
        product.articleCategory = category;
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
            throw new ApiException(404, "Artikel nicht gefunden.");
        }
        return product;
    }

    private Product lockProduct(Long id) {
        Product product = Product.<Product>find("id = ?1", id)
                .withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
        if (product == null) {
            throw new ApiException(404, "Artikel nicht gefunden.");
        }
        entityManager.refresh(product);
        return product;
    }

    private Product findForReadOrThrow(Long id) {
        Product product = Product.<Product>find(
                "select p from Product p left join fetch p.articleCategory where p.id = ?1", id)
                .firstResult();
        if (product == null) {
            throw new ApiException(404, "Artikel nicht gefunden.");
        }
        return product;
    }
}
