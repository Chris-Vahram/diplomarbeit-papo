package at.papo.partnerportal.shop;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// The reason is shown to the partner; 10-500 visible characters (partner-shop spec).
public record ShopTakedownRequest(
        @NotNull(message = "Bitte einen Grund angeben.")
        @Pattern(regexp = "(?s)\\s*\\S.{8,}\\S\\s*", message = "Der Grund muss mindestens 10 Zeichen lang sein.")
        @Size(max = 500, message = "Der Grund darf höchstens 500 Zeichen lang sein.") String reason
) {
}
