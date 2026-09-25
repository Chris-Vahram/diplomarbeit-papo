package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.ApiException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerTradeProofValidatorTest {

    @Test
    void acceptsVerifiedPdfPngAndJpeg() {
        assertEquals("application/pdf", PartnerTradeProofValidator.validate(
                "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII), "nachweis.pdf", "application/pdf"));
        assertEquals("image/png", PartnerTradeProofValidator.validate(png(), "nachweis.png", "image/png"));
        assertEquals("image/jpeg", PartnerTradeProofValidator.validate(
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9 }, "nachweis.jpg", "image/jpeg"));
    }

    @Test
    void rejectsForgedSignaturesAndMimeMismatch() {
        assertThrows(ApiException.class, () -> PartnerTradeProofValidator.validate(
                "%PDF-just-a-header".getBytes(StandardCharsets.US_ASCII), "nachweis.pdf", "application/pdf"));
        assertThrows(ApiException.class, () -> PartnerTradeProofValidator.validate(
                png(), "nachweis.pdf", "application/pdf"));
        assertThrows(ApiException.class, () -> PartnerTradeProofValidator.validate(
                png(), "nachweis.png", "application/pdf"));
    }

    private byte[] png() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 13, 'I', 'H', 'D', 'R',
                0, 0, 0, 1, 0, 0, 0, 1
        };
    }
}
