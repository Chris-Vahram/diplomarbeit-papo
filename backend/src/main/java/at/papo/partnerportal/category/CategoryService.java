package at.papo.partnerportal.category;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.security.CurrentCompany;
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

    @Inject
    CurrentCompany currentCompany;

    public List<CategoryDto> list() {
        return Category.<Category>find("company = ?1 order by name, id", currentCompany.resolve()).list().stream()
                .map(CategoryDto::from)
                .toList();
    }

    @Transactional
    public CategoryDto create(CategoryCreateRequest request) {
        String name = CategoryNames.requireValid(request.name());
        String normalizedName = name.toLowerCase(Locale.ROOT);
        CompanyProfile company = currentCompany.resolve();
        lockAll(company);
        if (Category.count("company = ?1 and normalizedName = ?2", company, normalizedName) > 0) {
            throw new ApiException(409, "Artikelkategorie bereits vorhanden.");
        }
        Category category = new Category();
        category.company = company;
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
        CompanyProfile company = currentCompany.resolve();
        List<Category> lockedCategories = lockAll(company);
        Category category = lockedCategories.stream().filter(item -> Objects.equals(item.id, id)).findFirst()
                .orElseThrow(() -> new ApiException(404, "Artikelkategorie nicht gefunden."));
        String name = CategoryNames.requireValid(request.name());
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (Category.count("company = ?1 and normalizedName = ?2 and id <> ?3", company, normalizedName, id) > 0) {
            throw new ApiException(409, "Artikelkategorie bereits vorhanden.");
        }
        category.name = name;
        category.normalizedName = normalizedName;
        category.active = request.active();
        return CategoryDto.from(category);
    }

    public Category findForProductInput(Long categoryId, String legacyName) {
        return findForProductInput(currentCompany.resolve(), categoryId, legacyName);
    }

    // Foreign categories behave like unknown ones (add-company-tenancy D8).
    public Category findForProductInput(CompanyProfile company, Long categoryId, String legacyName) {
        if (categoryId != null && legacyName != null && !legacyName.isBlank()) {
            throw new ApiException(400, "Artikelkategorie darf entweder über ID oder Namen angegeben werden.");
        }
        if (categoryId != null) {
            if (categoryId <= 0) {
                throw new ApiException(400, "Artikelkategorie nicht gefunden.");
            }
            Category category = Category.find("id = ?1 and company = ?2", categoryId, company).firstResult();
            if (category == null) {
                throw new ApiException(400, "Artikelkategorie nicht gefunden.");
            }
            return category;
        }
        if (legacyName == null || legacyName.isBlank()) {
            return null;
        }
        String normalizedName = CategoryNames.normalized(legacyName);
        Category category = Category.find("company = ?1 and normalizedName = ?2", company, normalizedName).firstResult();
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

    // Serialises catalogue edits of one company: locking the company row also covers the case
    // of a company without categories; other companies' catalogues stay unlocked.
    private List<Category> lockAll(CompanyProfile company) {
        entityManager.lock(entityManager.contains(company) ? company
                : entityManager.find(CompanyProfile.class, company.id), LockModeType.PESSIMISTIC_WRITE);
        List<Category> categories = Category.<Category>find("company = ?1 order by id", company)
                .withLock(LockModeType.PESSIMISTIC_WRITE).list();
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
