package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeRegisterIdentifierValidatorTest {

    private final TradeRegisterIdentifierValidator validator = new TradeRegisterIdentifierValidator();

    @Test
    void normalizesAustrianGisaIdentifier() {
        TradeRegisterIdentifier result = validator.normalize(
                new TradeRegisterIdentifierRequest(" at ", TradeRegisterIdentifierType.GISA, " 12345678 "));

        assertEquals("AT", result.country);
        assertEquals(TradeRegisterIdentifierType.GISA, result.type);
        assertEquals("12345678", result.value);
    }

    @Test
    void requiresCountrySpecificRegisterType() {
        assertThrows(ApiException.class, () -> validator.normalize(
                new TradeRegisterIdentifierRequest("AT", TradeRegisterIdentifierType.OTHER_TRADE_REGISTER, "123")));
        assertThrows(ApiException.class, () -> validator.normalize(
                new TradeRegisterIdentifierRequest("DE", TradeRegisterIdentifierType.GISA, "123")));
    }

    @Test
    void rejectsBlankAndTooLongValues() {
        assertThrows(ApiException.class, () -> validator.normalize(
                new TradeRegisterIdentifierRequest("AT", TradeRegisterIdentifierType.GISA, " ")));
        assertThrows(ApiException.class, () -> validator.normalize(
                new TradeRegisterIdentifierRequest("AT", TradeRegisterIdentifierType.GISA, "x".repeat(65))));
    }
}
