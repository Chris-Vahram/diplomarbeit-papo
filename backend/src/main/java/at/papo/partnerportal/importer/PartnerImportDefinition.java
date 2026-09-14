package at.papo.partnerportal.importer;

import at.papo.partnerportal.business.BusinessTaxIdentifierRequest;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.business.BusinessTaxIdentifierValidator;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.partner.PartnerNumber;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static at.papo.partnerportal.importer.ImportValueParsing.blankToNull;
import static at.papo.partnerportal.importer.ImportValueParsing.isBlank;
import static at.papo.partnerportal.importer.ImportValueParsing.parseBoolean;
import static at.papo.partnerportal.importer.ImportValueParsing.parseDecimal;

@ApplicationScoped
public class PartnerImportDefinition implements ImportDefinition {

    @Inject
    BusinessTaxIdentifierValidator taxIdentifierValidator;

    private static final List<ImportField> FIELDS = List.of(
            new ImportField("partnerNumber", "Partnernummer", true,
                    List.of("partner nr", "partnernr", "nummer", "partner number", "kundennummer")),
            new ImportField("name", "Name", true, List.of("firma", "firmenname", "partnername")),
            new ImportField("street", "Straße", true, List.of("strasse", "straße")),
            new ImportField("houseNumber", "Hausnummer", true, List.of("hausnr", "nr", "hausnummer")),
            new ImportField("postalCode", "PLZ", true, List.of("plz", "postleitzahl")),
            new ImportField("city", "Ort", true, List.of("stadt", "ort")),
            new ImportField("country", "Land", true, List.of("land", "country")),
            new ImportField("taxCountry", "Steuerland", true,
                    List.of("steuerland", "tax country")),
            new ImportField("taxIdentifierType", "Steuerart", true,
                    List.of("steuerart", "tax type", "steueridentifikationstyp")),
            new ImportField("taxIdentifierValue", "Steueridentifikationswert", true,
                    List.of("steuernummer", "uid", "uid nummer", "tax id", "tax number")),
            new ImportField("email", "E-Mail", false, List.of("email", "mail")),
            new ImportField("phone", "Telefon", false, List.of("tel", "telefonnummer")),
            new ImportField("commissionPercent", "Provision (%)", true,
                    List.of("provision", "commission", "prozent")),
            new ImportField("active", "Aktiv", false, List.of("status", "aktiviert"))
    );

    @Override
    public List<ImportField> fields() {
        return FIELDS;
    }

    @Override
    public List<String> validateRow(Map<String, String> mappedValues) {
        List<String> errors = new ArrayList<>();
        if (isBlank(mappedValues.get("partnerNumber"))) {
            errors.add("Partnernummer fehlt.");
        }
        if (isBlank(mappedValues.get("name"))) {
            errors.add("Name fehlt.");
        }
        if (isBlank(mappedValues.get("street"))) {
            errors.add("Straße fehlt.");
        }
        if (isBlank(mappedValues.get("houseNumber"))) {
            errors.add("Hausnummer fehlt.");
        }
        if (isBlank(mappedValues.get("postalCode"))) {
            errors.add("PLZ fehlt.");
        }
        if (isBlank(mappedValues.get("city"))) {
            errors.add("Ort fehlt.");
        }
        if (isBlank(mappedValues.get("country"))) {
            errors.add("Land fehlt.");
        }

        if (isBlank(mappedValues.get("taxCountry")) || isBlank(mappedValues.get("taxIdentifierType"))
                || isBlank(mappedValues.get("taxIdentifierValue"))) {
            errors.add("Steueridentifikation muss vollständig angegeben werden.");
        } else {
            try {
                BusinessTaxIdentifierType type = BusinessTaxIdentifierType.fromImportValue(
                        mappedValues.get("taxIdentifierType"));
                taxIdentifierValidator.normalize(new BusinessTaxIdentifierRequest(
                        mappedValues.get("taxCountry"), type, mappedValues.get("taxIdentifierValue")));
            } catch (IllegalArgumentException e) {
                errors.add("Steuerart ist ungültig.");
            } catch (ApiException e) {
                errors.add(e.getMessage());
            }
        }

        String commissionPercent = mappedValues.get("commissionPercent");
        if (isBlank(commissionPercent)) {
            errors.add("Provision fehlt.");
        } else {
            try {
                if (parseDecimal(commissionPercent).compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Provision darf nicht negativ sein.");
                }
            } catch (NumberFormatException e) {
                errors.add("Provision ist keine gültige Zahl.");
            }
        }

        String active = mappedValues.get("active");
        if (!isBlank(active) && parseBoolean(active) == null) {
            errors.add("Aktiv muss ja/nein bzw. true/false sein.");
        }

        return errors;
    }

    @Override
    public String identifierValue(Map<String, String> mappedValues) {
        return mappedValues.get("partnerNumber") != null ? mappedValues.get("partnerNumber").trim() : null;
    }

    // Case-insensitive to match the in-file duplicate check (already lowercased)
    // and the product definition's lower(name) pattern. Locale.ROOT matches
    // PartnerNumber.canonicalize's case-folding (avoids the Turkish-I trap).
    @Override
    public boolean existsInDb(String identifierValue) {
        return Partner.count("lower(partnerNumber) = ?1", identifierValue.toLowerCase(Locale.ROOT)) > 0;
    }

    @Override
    public void persist(Map<String, String> mappedValues) {
        Partner partner = new Partner();
        partner.partnerNumber = PartnerNumber.canonicalize(mappedValues.get("partnerNumber"));
        partner.name = mappedValues.get("name").trim();
        partner.address = new Address(mappedValues.get("street").trim(), mappedValues.get("houseNumber").trim(),
                mappedValues.get("postalCode").trim(), mappedValues.get("city").trim(),
                mappedValues.get("country").trim());
        partner.email = blankToNull(mappedValues.get("email"));
        partner.phone = blankToNull(mappedValues.get("phone"));
        BusinessTaxIdentifierType type = BusinessTaxIdentifierType.fromImportValue(
                mappedValues.get("taxIdentifierType"));
        partner.taxIdentifier = taxIdentifierValidator.normalize(new BusinessTaxIdentifierRequest(
                mappedValues.get("taxCountry"), type, mappedValues.get("taxIdentifierValue")));
        partner.commissionPercent = parseDecimal(mappedValues.get("commissionPercent"));
        Boolean active = parseBoolean(mappedValues.get("active"));
        partner.active = active == null || active;
        partner.persist();
    }
}
