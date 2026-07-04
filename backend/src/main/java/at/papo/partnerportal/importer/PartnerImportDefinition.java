package at.papo.partnerportal.importer;

import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static at.papo.partnerportal.importer.ImportValueParsing.blankToNull;
import static at.papo.partnerportal.importer.ImportValueParsing.isBlank;
import static at.papo.partnerportal.importer.ImportValueParsing.parseBoolean;
import static at.papo.partnerportal.importer.ImportValueParsing.parseDecimal;

@ApplicationScoped
public class PartnerImportDefinition implements ImportDefinition {

    private static final List<ImportField> FIELDS = List.of(
            new ImportField("partnerNumber", "Partnernummer", true,
                    List.of("partner nr", "partnernr", "nummer", "partner number", "kundennummer")),
            new ImportField("name", "Name", true, List.of("firma", "firmenname", "partnername")),
            new ImportField("address", "Adresse", false, List.of("anschrift", "strasse")),
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
    // and the product definition's lower(name) pattern.
    @Override
    public boolean existsInDb(String identifierValue) {
        return Partner.count("lower(partnerNumber) = ?1", identifierValue.toLowerCase()) > 0;
    }

    @Override
    public void persist(Map<String, String> mappedValues) {
        Partner partner = new Partner();
        partner.partnerNumber = mappedValues.get("partnerNumber").trim();
        partner.name = mappedValues.get("name").trim();
        partner.address = blankToNull(mappedValues.get("address"));
        partner.email = blankToNull(mappedValues.get("email"));
        partner.phone = blankToNull(mappedValues.get("phone"));
        partner.commissionPercent = parseDecimal(mappedValues.get("commissionPercent"));
        Boolean active = parseBoolean(mappedValues.get("active"));
        partner.active = active == null || active;
        partner.persist();
    }
}
