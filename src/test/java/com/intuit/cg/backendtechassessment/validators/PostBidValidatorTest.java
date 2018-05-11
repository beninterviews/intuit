package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.PostBidDetails;
import javax.ws.rs.WebApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PostBidValidatorTest {
    private final PostBidValidator validator = new PostBidValidator();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRequestOkay() {
        PostBidDetails details = new PostBidDetails(100L);

        validator.validate(details);
    }

    @Test
    public void testValueRequired() {
        PostBidDetails details = new PostBidDetails(null);

        WebApplicationException expectedError = ApiErrors.required("The value of the bid");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testBidIsValid() {
        PostBidDetails details = new PostBidDetails(-10L);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }
}
