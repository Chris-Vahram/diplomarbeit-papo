package at.papo.partnerportal.business;

import java.util.Locale;

public enum BusinessTaxIdentifierType {
    VAT_ID,
    NATIONAL_TAX_ID;

    public static BusinessTaxIdentifierType fromImportValue(String value) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "VAT_ID", "UID", "VAT", "UST_ID" -> VAT_ID;
            case "NATIONAL_TAX_ID", "STEUERNUMMER", "NATIONAL", "TAX_ID" -> NATIONAL_TAX_ID;
            default -> throw new IllegalArgumentException("Unbekannte Steuerart");
        };
    }
}
