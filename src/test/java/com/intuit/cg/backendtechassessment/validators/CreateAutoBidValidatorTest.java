package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.CreateAutoBidDetails;
import javax.ws.rs.WebApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CreateAutoBidValidatorTest {
    private final CreateAutoBidValidator validator = new CreateAutoBidValidator();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRequestOkay() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(100L, 10L);

        validator.validate(details);
    }

    @Test
    public void testMinimumBidRequired() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(100L, null);

        WebApplicationException expectedError = ApiErrors.required("The minimum bid");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testStartingBidRequired() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(null, 10L);

        WebApplicationException expectedError = ApiErrors.required("The starting bid");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testBidIsValid() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(1L, 10L);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testMinimumBidIsValid() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(-10L, 10L);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testStartingBidIsValid() {
        CreateAutoBidDetails details = new CreateAutoBidDetails(10L, -10L);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }
}
