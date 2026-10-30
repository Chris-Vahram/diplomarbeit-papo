package at.papo.partnerportal.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

// add-company-tenancy migration. expand() (Release A) assigns company-owned rows without company
// to the single existing company. contract() (Release B) then makes company_id NOT NULL and drops
// the global single-column uniques and the singleton key, so a second company can exist. The
// result stays compatible with the A image, which the deploy gateway may restore without SQL.
// Physical names follow the default naming (no naming strategy): companyprofile, productimage.
@ApplicationScoped
public class CompanyTenancyMigrationService {

    static final List<String> OWNED_TABLES = List.of("partner", "product", "article_categories", "productimage");

    // Pre-tenancy global uniques (table, column); replaced by the composite (company_id, column) ones.
    static final List<List<String>> GLOBAL_UNIQUES = List.of(
            List.of("partner", "partnernumber"),
            List.of("article_categories", "normalized_name"),
            List.of("companyprofile", "singletonkey"));

    @Inject
    EntityManager entityManager;

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
    }

    // Idempotent; every step is a no-op on a schema that is already contracted.
    @Transactional
    public void contract() {
        expand();
        for (String table : OWNED_TABLES) {
            entityManager.createNativeQuery("ALTER TABLE " + table + " ALTER COLUMN company_id SET NOT NULL")
                    .executeUpdate();
        }
        for (List<String> unique : GLOBAL_UNIQUES) {
            for (Object name : uniqueConstraints(unique.get(0), unique.get(1))) {
                entityManager.createNativeQuery("ALTER TABLE " + unique.get(0) + " DROP CONSTRAINT \"" + name + "\"")
                        .executeUpdate();
            }
        }
        if (hasColumn("companyprofile", "singletonkey")) {
            entityManager.createNativeQuery("ALTER TABLE companyprofile ALTER COLUMN singletonkey DROP NOT NULL")
                    .executeUpdate();
        }
        if (uniqueConstraints("partner", "company_id", "partnernumber").isEmpty()
                || uniqueConstraints("article_categories", "company_id", "normalized_name").isEmpty()) {
            throw new IllegalStateException("Mandanten-Übernahme abgebrochen: Eindeutigkeit je Firma fehlt.");
        }
    }

    // Unique constraints of the table on exactly these columns (any order), in the current schema.
    private List<?> uniqueConstraints(String table, String... columns) {
        return entityManager.createNativeQuery("""
                select c.conname from pg_constraint c
                join pg_class t on t.oid = c.conrelid
                where c.contype = 'u' and t.relname = ?1 and t.relnamespace = current_schema()::regnamespace
                  and (select array_agg(a.attname::text order by a.attname) from pg_attribute a
                       where a.attrelid = t.oid and a.attnum = any (c.conkey))
                      = (select array_agg(x order by x) from unnest(string_to_array(?2, ',')) x)
                """).setParameter(1, table).setParameter(2, String.join(",", columns)).getResultList();
    }

    private boolean hasColumn(String table, String column) {
        return ((Number) entityManager.createNativeQuery("select count(*) from information_schema.columns "
                + "where table_schema = current_schema() and table_name = ?1 and column_name = ?2")
                .setParameter(1, table).setParameter(2, column).getSingleResult()).longValue() > 0;
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
