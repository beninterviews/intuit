package com.intuit.cg.backendtechassessment.validators;

import javax.ws.rs.WebApplicationException;

/**
 * Contract to implement when creating a new validator for an API.
 * It will take in parameter the object to validate and should throw a {@link javax.ws.rs.WebApplicationException} if the validation fails.
 * This is usually the first part of a two part validation system.
 * It's supposed to validate the sanity of the input data (values within range, required etc...).
 * The 2nd validation will usually validate against the overall system (is a value correct based on something that we need to get from our data store for example).
 *
 * @param <T> The type to validate.
 */
public interface ApiValidator<T> {
    void validate(T data) throws WebApplicationException;
}
