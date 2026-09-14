package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class BusinessTaxIdentifierValidator {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    private static final Pattern GENERIC_VALUE = Pattern.compile("[A-Z0-9 ._/-]{2,64}");
    private static final Pattern AUSTRIAN_UID = Pattern.compile("ATU[0-9]{8}");
    private static final Pattern AUSTRIAN_NATIONAL = Pattern.compile("[0-9]{9}");

    public BusinessTaxIdentifier normalize(BusinessTaxIdentifierRequest request) {
        if (request == null) {
            throw invalid("Steueridentifikation ist erforderlich.");
        }
        String country = request.country() == null ? "" : request.country().trim().toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRIES.contains(country) || !COUNTRY.matcher(country).matches()) {
            throw invalid("Steuerland muss ein gültiger ISO-Ländercode sein.");
        }
        if (request.type() == null) {
            throw invalid("Steuerart ist erforderlich.");
        }
        String value = request.value() == null ? "" : request.value().trim().toUpperCase(Locale.ROOT);
        if (value.isBlank()) {
            throw invalid("Steueridentifikationswert ist erforderlich.");
        }
        if ("AT".equals(country) && request.type() == BusinessTaxIdentifierType.VAT_ID
                && !AUSTRIAN_UID.matcher(value).matches()) {
            throw invalid("Eine österreichische UID muss mit ATU und acht Ziffern angegeben werden.");
        }
        if ("AT".equals(country) && request.type() == BusinessTaxIdentifierType.NATIONAL_TAX_ID
                && !AUSTRIAN_NATIONAL.matcher(value).matches()) {
            throw invalid("Eine österreichische Steuernummer muss genau neun Ziffern enthalten.");
        }
        if (!("AT".equals(country) && ((request.type() == BusinessTaxIdentifierType.VAT_ID
                && AUSTRIAN_UID.matcher(value).matches())
                || (request.type() == BusinessTaxIdentifierType.NATIONAL_TAX_ID
                        && AUSTRIAN_NATIONAL.matcher(value).matches())))
                && !GENERIC_VALUE.matcher(value).matches()) {
            throw invalid("Steueridentifikationswert enthält ungültige Zeichen.");
        }
        return new BusinessTaxIdentifier(country, request.type(), value);
    }

    private ApiException invalid(String message) {
        return new ApiException(400, message);
    }
}
