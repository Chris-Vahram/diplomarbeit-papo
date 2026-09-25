package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class TradeRegisterIdentifierValidator {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");

    public TradeRegisterIdentifier normalize(TradeRegisterIdentifierRequest request) {
        if (request == null) {
            throw invalid("Registerkennzeichen ist erforderlich.");
        }
        String country = request.country() == null ? "" : request.country().trim().toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRIES.contains(country) || !COUNTRY.matcher(country).matches()) {
            throw invalid("Registerland muss ein gültiger ISO-Ländercode sein.");
        }
        if (request.type() == null) {
            throw invalid("Registerart ist erforderlich.");
        }
        if ("AT".equals(country) && request.type() != TradeRegisterIdentifierType.GISA) {
            throw invalid("Für Österreich ist ausschließlich GISA zulässig.");
        }
        if (!"AT".equals(country) && request.type() != TradeRegisterIdentifierType.OTHER_TRADE_REGISTER) {
            throw invalid("Außerhalb Österreichs ist ausschließlich OTHER_TRADE_REGISTER zulässig.");
        }
        String value = request.value() == null ? "" : request.value().trim();
        if (value.isBlank()) {
            throw invalid("Registerkennzeichen ist erforderlich.");
        }
        if (value.length() > 64) {
            throw invalid("Registerkennzeichen darf höchstens 64 Zeichen enthalten.");
        }
        return new TradeRegisterIdentifier(country, request.type(), value);
    }

    private ApiException invalid(String message) {
        return new ApiException(400, message);
    }
}
