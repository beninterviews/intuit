package com.intuit.cg.backendtechassessment.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class WebApplicationExceptions {
    public static WebApplicationException internalError() {
        return new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    public static WebApplicationException invalidParameter(String message) {
        return new WebApplicationException(message, Status.BAD_REQUEST.getStatusCode());
    }

    public static WebApplicationException notFound(String message) {
        return new WebApplicationException(message, Status.NOT_FOUND.getStatusCode());
    }
}
