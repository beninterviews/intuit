package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.CreateUserDetails;
import javax.ws.rs.WebApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CreateUserValidatorTest {
    private final CreateUserValidator validator = new CreateUserValidator();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRequestOkay() {
        CreateUserDetails details = new CreateUserDetails("test", "pwd", "pwd", "email");

        validator.validate(details);
    }

    @Test
    public void testNameRequired() {
        CreateUserDetails details = new CreateUserDetails(null, "pwd", "pwd", "email");

        WebApplicationException expectedError = ApiErrors.required("The name");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testPasswordRequired() {
        CreateUserDetails details = new CreateUserDetails("test", null, "pwd", "email");

        WebApplicationException expectedError = ApiErrors.required("The password");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testConfirmationPasswordRequired() {
        CreateUserDetails details = new CreateUserDetails("test", "pwd", null, "email");

        WebApplicationException expectedError = ApiErrors.required("The confirmation password");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testEmailRequired() {
        CreateUserDetails details = new CreateUserDetails("test", "pwd", "pwd", null);

        WebApplicationException expectedError = ApiErrors.required("The email");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testPasswordShouldMatch() {
        CreateUserDetails details = new CreateUserDetails("test", "pwd", "pwd2", "email");

        WebApplicationException expectedError = ApiErrors.passwordDoNotMatch();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }
}
