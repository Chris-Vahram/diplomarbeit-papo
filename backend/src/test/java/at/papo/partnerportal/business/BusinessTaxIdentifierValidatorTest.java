package at.papo.partnerportal.business;

import at.papo.partnerportal.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessTaxIdentifierValidatorTest {

    private final BusinessTaxIdentifierValidator validator = new BusinessTaxIdentifierValidator();

    @Test
    void normalizesCountryAndValue() {
        BusinessTaxIdentifier result = validator.normalize(
                new BusinessTaxIdentifierRequest(" at ", BusinessTaxIdentifierType.VAT_ID, " atu12345678 "));

        assertEquals("AT", result.country);
        assertEquals("ATU12345678", result.value);
    }

    @Test
    void acceptsAustrianNationalTaxNumberWithoutUid() {
        BusinessTaxIdentifier result = validator.normalize(
                new BusinessTaxIdentifierRequest("AT", BusinessTaxIdentifierType.NATIONAL_TAX_ID, "123456789"));

        assertEquals("123456789", result.value);
    }

    @Test
    void rejectsInvalidAustrianSyntaxAndPartialData() {
        assertThrows(ApiException.class, () -> validator.normalize(
                new BusinessTaxIdentifierRequest("AT", BusinessTaxIdentifierType.VAT_ID, "AT12345678")));
        assertThrows(ApiException.class, () -> validator.normalize(
                new BusinessTaxIdentifierRequest("AT", BusinessTaxIdentifierType.NATIONAL_TAX_ID, "12345678")));
        assertThrows(ApiException.class, () -> validator.normalize(
                new BusinessTaxIdentifierRequest("DE", null, "DE123456789")));
    }

    @Test
    void acceptsGenericSyntaxAndRejectsUnsupportedCharacters() {
        String maxLength = "A".repeat(64);
        assertEquals(maxLength, validator.normalize(
                new BusinessTaxIdentifierRequest("DE", BusinessTaxIdentifierType.VAT_ID, maxLength)).value);
        assertThrows(ApiException.class, () -> validator.normalize(
                new BusinessTaxIdentifierRequest("DE", BusinessTaxIdentifierType.VAT_ID, maxLength + "A")));
        assertEquals("DE123456789", validator.normalize(
                new BusinessTaxIdentifierRequest("DE", BusinessTaxIdentifierType.VAT_ID, "de123456789")).value);
        assertThrows(ApiException.class, () -> validator.normalize(
                new BusinessTaxIdentifierRequest("DE", BusinessTaxIdentifierType.VAT_ID, "DE/12?")));
    }
}
