package at.papo.partnerportal.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;

// Release A ("expand") of add-company-tenancy: assigns company-owned rows without company to the
// single existing company and keeps the schema compatible with the previous image (company_id
// stays nullable, old unique constraints and the 'CURRENT' singleton key stay). The deploy
// gateway may restore the previous image without touching SQL, so nothing here may break it.
// Physical names follow the default naming (no naming strategy): companyprofile, productimage.
@ApplicationScoped
public class CompanyTenancyMigrationService {

    static final List<String> OWNED_TABLES = List.of("partner", "product", "article_categories", "productimage");

    @Inject
    EntityManager entityManager;

    // Legacy deployment-wide recipient (PAPO_MAIL_COMPANY_RECIPIENT); copied once into company 1.
    @ConfigProperty(name = "papo.mail.company-recipient")
    Optional<String> legacyCompanyRecipient;

    @Transactional
    public void expand() {
        entityManager.createNativeQuery("LOCK TABLE companyprofile, partner, product, article_categories, productimage "
                + "IN ACCESS EXCLUSIVE MODE").executeUpdate();
        long unassigned = unassignedRows();
        if (unassigned > 0) {
            List<?> profileIds = entityManager.createNativeQuery("select id from companyprofile").getResultList();
            if (profileIds.isEmpty()) {
                throw new IllegalStateException("Mandanten-Übernahme abgebrochen: Daten ohne Firmenprofil.");
            }
            if (profileIds.size() > 1) {
                throw new IllegalStateException(
                        "Mandanten-Übernahme abgebrochen: mehrere Firmen, nicht zuordenbare Daten.");
            }
            Long companyId = ((Number) profileIds.get(0)).longValue();
            for (String table : OWNED_TABLES) {
                entityManager.createNativeQuery("update " + table + " set company_id = ?1 where company_id is null")
                        .setParameter(1, companyId)
                        .executeUpdate();
            }
            if (unassignedRows() != 0) {
                throw new IllegalStateException("Mandanten-Übernahme abgebrochen: Zuordnung unvollständig.");
            }
        }
        legacyCompanyRecipient.map(String::trim).filter(value -> !value.isEmpty()).ifPresent(recipient ->
                entityManager.createNativeQuery("update companyprofile set ordernoticeemail = ?1 "
                                + "where singletonkey = ?2 and ordernoticeemail is null")
                        .setParameter(1, recipient)
                        .setParameter(2, CompanyProfile.CURRENT_KEY)
                        .executeUpdate());
    }

    private long unassignedRows() {
        long total = 0;
        for (String table : OWNED_TABLES) {
            total += ((Number) entityManager.createNativeQuery(
                    "select count(*) from " + table + " where company_id is null").getSingleResult()).longValue();
        }
        return total;
    }
}
