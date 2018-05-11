package com.intuit.cg.backendtechassessment.constraints;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import java.util.StringJoiner;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.model.Invocable;

@Slf4j
@Provider
public class ApiConstraintViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    @Override
    public Response toResponse(JerseyViolationException exception) {
        String responseMessage = exception.getMessage();

        if (exception.getConstraintViolations().size() > 0) {
            StringJoiner message = new StringJoiner("; ");

            for (ConstraintViolation<?> v : exception.getConstraintViolations()) {
                // If the violation comes from our ApiConstraint, just use the constraint message.
                if (v.getConstraintDescriptor().getAnnotation().annotationType() == ApiConstraint.class) {
                    message.add(v.getMessage());
                } else {
                    final Invocable invocable = exception.getInvocable();
                    message.add(ConstraintMessage.getMessage(v, invocable));
                }
            }

            responseMessage = message.toString();
        }

        return Response.status(Status.BAD_REQUEST)
                .entity(new ApiConstraintError(Status.BAD_REQUEST.getStatusCode(), responseMessage))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @Value
    private static class ApiConstraintError {
        private final int code;
        private final String message;
    }
}
