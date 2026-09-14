package at.papo.partnerportal.category;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
public class CategoryService {

    @Inject
    EntityManager entityManager;

    public List<CategoryDto> list() {
        return Category.<Category>find("order by name, id").list().stream()
                .map(CategoryDto::from)
                .toList();
    }

    @Transactional
    public CategoryDto create(CategoryCreateRequest request) {
        String name = CategoryNames.requireValid(request.name());
        String normalizedName = name.toLowerCase(Locale.ROOT);
        lockAll();
        if (Category.count("normalizedName", normalizedName) > 0) {
            throw new ApiException(409, "Artikelkategorie bereits vorhanden.");
        }
        Category category = new Category();
        category.name = name;
        category.normalizedName = normalizedName;
        category.active = true;
        try {
            category.persistAndFlush();
        } catch (PersistenceException exception) {
            throw new ApiException(409, "Artikelkategorie bereits vorhanden.");
        }
        return CategoryDto.from(category);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryUpdateRequest request) {
        List<Category> lockedCategories = lockAll();
        Category category = lockedCategories.stream().filter(item -> Objects.equals(item.id, id)).findFirst()
                .orElseThrow(() -> new ApiException(404, "Artikelkategorie nicht gefunden."));
        String name = CategoryNames.requireValid(request.name());
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (Category.count("normalizedName = ?1 and id <> ?2", normalizedName, id) > 0) {
            throw new ApiException(409, "Artikelkategorie bereits vorhanden.");
        }
        List<at.papo.partnerportal.product.Product> products = at.papo.partnerportal.product.Product
                .<at.papo.partnerportal.product.Product>find("articleCategory = ?1 order by id", category)
                .withLock(LockModeType.PESSIMISTIC_WRITE).list();
        category.name = name;
        category.normalizedName = normalizedName;
        category.active = request.active();
        products.forEach(product -> product.category = category.name);
        return CategoryDto.from(category);
    }

    public Category resolveForProduct(Long categoryId, String legacyName) {
        Category category = findForProductInput(categoryId, legacyName);
        if (category != null && !category.active) {
            throw new ApiException(400, "Artikelkategorie ist deaktiviert.");
        }
        return category;
    }

    public Category findForProductInput(Long categoryId, String legacyName) {
        if (categoryId != null && legacyName != null && !legacyName.isBlank()) {
            throw new ApiException(400, "Artikelkategorie darf entweder über ID oder Namen angegeben werden.");
        }
        if (categoryId != null) {
            if (categoryId <= 0) {
                throw new ApiException(400, "Artikelkategorie nicht gefunden.");
            }
            Category category = Category.findById(categoryId);
            if (category == null) {
                throw new ApiException(400, "Artikelkategorie nicht gefunden.");
            }
            return category;
        }
        if (legacyName == null || legacyName.isBlank()) {
            return null;
        }
        String normalizedName = CategoryNames.normalized(legacyName);
        Category category = Category.find("normalizedName", normalizedName).firstResult();
        if (category == null) {
            throw new ApiException(400, "Artikelkategorie nicht gefunden.");
        }
        return category;
    }

    public List<Category> lockCategories(Collection<Long> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().sorted().map(id -> {
            Category category = Category.<Category>find("id = ?1", id)
                    .withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
            if (category == null) {
                throw new ApiException(400, "Artikelkategorie nicht gefunden.");
            }
            entityManager.refresh(category);
            return category;
        }).toList();
    }

    private List<Category> lockAll() {
        List<Category> categories = Category.<Category>find("order by id").withLock(LockModeType.PESSIMISTIC_WRITE).list();
        categories.forEach(entityManager::refresh);
        return categories;
    }

    public Category lockedCategory(List<Category> categories, Long id) {
        if (id == null) {
            return null;
        }
        return categories.stream().filter(category -> Objects.equals(category.id, id)).findFirst()
                .orElseThrow(() -> new ApiException(400, "Artikelkategorie nicht gefunden."));
    }
}
