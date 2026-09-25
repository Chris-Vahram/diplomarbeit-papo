package at.papo.partnerportal.business;

import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class BusinessTaxIdentifierMigrationService {

    private static final Map<String, String> DEMO_IDENTIFIERS = Map.of(
            "PARTNER-DEMO", "ATU12345678",
            "P-1002", "ATU23456789",
            "P-1003", "ATU34567890");

    @Inject
    EntityManager entityManager;

    @Inject
    BusinessTaxIdentifierValidator validator;

    @Transactional
    public void lockPartnerTable() {
        entityManager.createNativeQuery("LOCK TABLE partner IN ACCESS EXCLUSIVE MODE").executeUpdate();
    }

    @Transactional
    public void migrate(boolean demoDataEnabled) {
        lockPartnerTable();
        List<Partner> partners = Partner.list("order by id");

        boolean needsBackfill = partners.stream().anyMatch(partner -> !isComplete(partner.taxIdentifier));
        if (needsBackfill && (!demoDataEnabled || !isVerifiedDemoDataset(partners))) {
            throw new IllegalStateException(
                    "Steueridentifikations-Übernahme abgebrochen: Datenbestand ist nicht als Demodaten verifiziert.");
        }

        for (Partner partner : partners) {
            if (isComplete(partner.taxIdentifier)) {
                validate(partner.taxIdentifier, "Partner " + partner.partnerNumber);
                continue;
            }
            if (!isEmpty(partner.taxIdentifier)) {
                throw migrationFailure(partner.partnerNumber);
            }
            partner.taxIdentifier = new BusinessTaxIdentifier(
                    "AT", BusinessTaxIdentifierType.VAT_ID, DEMO_IDENTIFIERS.get(partner.partnerNumber));
        }

        initializeCompanyProfile(partners, demoDataEnabled);
        entityManager.flush();

        if (partners.stream().anyMatch(partner -> !isComplete(partner.taxIdentifier))) {
            throw new IllegalStateException("Steueridentifikations-Übernahme abgebrochen: Unvollständige Partnerdaten.");
        }

        for (String column : new String[] { "tax_country", "tax_identifier_type", "tax_identifier_value" }) {
            entityManager.createNativeQuery("ALTER TABLE partner ALTER COLUMN " + column + " SET NOT NULL")
                    .executeUpdate();
        }
    }

    private void initializeCompanyProfile(List<Partner> partners, boolean demoDataEnabled) {
        CompanyProfile current = CompanyProfile.find("singletonKey", CompanyProfile.CURRENT_KEY).firstResult();
        if (current != null) {
            validate(current.taxIdentifier, "Firmenprofil");
            return;
        }
        if (CompanyProfile.count() != 0 || !demoDataEnabled || !isVerifiedDemoDataset(partners)) {
            throw new IllegalStateException("Steueridentifikations-Übernahme abgebrochen: Firmenprofil ist inkonsistent.");
        }

        CompanyProfile profile = new CompanyProfile();
        profile.singletonKey = CompanyProfile.CURRENT_KEY;
        profile.businessName = "Huemer GmbH";
        profile.taxIdentifier = new BusinessTaxIdentifier(
                "AT", BusinessTaxIdentifierType.VAT_ID, "ATU87654321");
        profile.tradeRegisterIdentifier = new TradeRegisterIdentifier(
                "AT", TradeRegisterIdentifierType.GISA, "12345678");
        profile.persist();
    }

    private boolean isVerifiedDemoDataset(List<Partner> partners) {
        Set<String> partnerNumbers = partners.stream()
                .map(partner -> partner.partnerNumber)
                .collect(Collectors.toSet());
        return partners.size() == DEMO_IDENTIFIERS.size() && partnerNumbers.equals(DEMO_IDENTIFIERS.keySet())
                && (hasName(partners, "PARTNER-DEMO", "Huemer GmbH")
                        || hasName(partners, "PARTNER-DEMO", "Elektro Huber GmbH"))
                && hasName(partners, "P-1002", "Sanitär Bauer KG")
                && hasName(partners, "P-1003", "Baumarkt Weber e.U.");
    }

    private boolean hasName(List<Partner> partners, String partnerNumber, String name) {
        return partners.stream().anyMatch(partner -> partnerNumber.equals(partner.partnerNumber)
                && name.equals(partner.name));
    }

    private void validate(BusinessTaxIdentifier identifier, String owner) {
        if (!isComplete(identifier)) {
            throw new IllegalStateException("Steueridentifikations-Übernahme abgebrochen: " + owner
                    + " hat unvollständige Daten.");
        }
        try {
            BusinessTaxIdentifier normalized = validator.normalize(
                    new BusinessTaxIdentifierRequest(identifier.country, identifier.type, identifier.value));
            if (!normalized.country.equals(identifier.country) || normalized.type != identifier.type
                    || !normalized.value.equals(identifier.value)) {
                throw new IllegalStateException("nicht kanonisch");
            }
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Steueridentifikations-Übernahme abgebrochen: " + owner
                    + " hat ungültige Daten.", exception);
        }
    }

    private boolean isComplete(BusinessTaxIdentifier identifier) {
        return identifier != null && identifier.country != null && identifier.type != null && identifier.value != null;
    }

    private boolean isEmpty(BusinessTaxIdentifier identifier) {
        return identifier == null
                || (identifier.country == null && identifier.type == null && identifier.value == null);
    }

    private IllegalStateException migrationFailure(String partnerNumber) {
        return new IllegalStateException(
                "Steueridentifikations-Übernahme abgebrochen: Keine verifizierten Daten für Partner "
                        + partnerNumber + ".");
    }
}
