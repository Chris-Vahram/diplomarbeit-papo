package at.papo.partnerportal.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Converts Bean Validation failures (@Valid on request DTOs) into the same
// German, plain-language error shape as ApiExceptionMapper.
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", "Eingabe ungültig.", "details", details))
                .build();
    }
}
