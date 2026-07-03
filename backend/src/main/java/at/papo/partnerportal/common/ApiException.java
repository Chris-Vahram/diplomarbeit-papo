package at.papo.partnerportal.common;

// Carries a plain-language (German) message for the ApiExceptionMapper — see
// docs/CONVENTIONS.md "Validation/error messages: German, plain language".
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
