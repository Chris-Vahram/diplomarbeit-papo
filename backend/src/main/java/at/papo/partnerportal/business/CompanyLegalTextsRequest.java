package at.papo.partnerportal.business;

import jakarta.validation.constraints.Size;

// All four texts are saved together; null or blank clears a text (placeholder again).
public record CompanyLegalTextsRequest(
        @Size(max = LegalText.MAX_LENGTH, message = "Das Impressum darf höchstens 30.000 Zeichen lang sein.") String impressum,
        @Size(max = LegalText.MAX_LENGTH, message = "Die AGB dürfen höchstens 30.000 Zeichen lang sein.") String agb,
        @Size(max = LegalText.MAX_LENGTH, message = "Die Datenschutzerklärung darf höchstens 30.000 Zeichen lang sein.") String datenschutz,
        @Size(max = LegalText.MAX_LENGTH, message = "Die Widerrufsbelehrung darf höchstens 30.000 Zeichen lang sein.") String widerruf
) {
}
