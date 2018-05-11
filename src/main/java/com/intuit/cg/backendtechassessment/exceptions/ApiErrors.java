package com.intuit.cg.backendtechassessment.exceptions;

import javax.ws.rs.WebApplicationException;

public class ApiErrors {
    public static WebApplicationException required(String object) {
        return WebApplicationExceptions.invalidParameter(String.format("%s is required.", object));
    }

    public static WebApplicationException invalidClosingBidTime() {
        return WebApplicationExceptions.invalidParameter("The supplied time is invalid. Date should be in the future.");
    }

    public static WebApplicationException invalidBudget() {
        return WebApplicationExceptions.invalidParameter("The budget is invalid.");
    }

    public static WebApplicationException invalidBid() {
        return WebApplicationExceptions.invalidParameter("The bid is invalid.");
    }

    public static WebApplicationException unknownProject(String id) {
        return WebApplicationExceptions.notFound(String.format("Project %s does not exist.", id));
    }

    public static WebApplicationException passwordDoNotMatch() {
        return WebApplicationExceptions.invalidParameter("Passwords don't match.");
    }

    public static WebApplicationException usernameAlreadyExists() {
        return WebApplicationExceptions.invalidParameter("A user with the same username already exists.");
    }
}
