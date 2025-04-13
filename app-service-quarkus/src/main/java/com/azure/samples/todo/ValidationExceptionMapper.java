package com.azure.samples.todo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        StringBuilder errorValueBuilder = new StringBuilder();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errorValueBuilder.append("\""+ violation.getMessage() + "\", ");
        }

        String errors = errorValueBuilder.toString();
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{\"errors\": ["
                           + errors.substring(0, errors.length() - 2) 
                           + "]}")
                       .build();
    }
}
