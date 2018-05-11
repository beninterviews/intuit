package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.CreateProjectDetails;
import java.time.OffsetDateTime;
import javax.ws.rs.WebApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CreateProjectValidatorTest {
    private final CreateProjectValidator validator = new CreateProjectValidator();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRequestOkay() {
        CreateProjectDetails details = new CreateProjectDetails("test", "test", 1000L, OffsetDateTime.now().plusDays(1));

        validator.validate(details);
    }

    @Test
    public void testNameRequired() {
        CreateProjectDetails details = new CreateProjectDetails(null, "test", 1000L, OffsetDateTime.now().plusDays(1));

        WebApplicationException expectedError = ApiErrors.required("The name");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testDescriptionRequired() {
        CreateProjectDetails details = new CreateProjectDetails("test", null, 1000L, OffsetDateTime.now().plusDays(1));

        WebApplicationException expectedError = ApiErrors.required("The description");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testBudgetRequired() {
        CreateProjectDetails details = new CreateProjectDetails("test", "test", null, OffsetDateTime.now().plusDays(1));

        WebApplicationException expectedError = ApiErrors.required("The budget");
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testDateRequired() {
        CreateProjectDetails details = new CreateProjectDetails("test", "test", 1000L, null);

        WebApplicationException expectedError = ApiErrors.invalidClosingBidTime();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }

    @Test
    public void testDateInFuture() {
        CreateProjectDetails details = new CreateProjectDetails("test", "test", 1000L, OffsetDateTime.now().minusDays(50));

        WebApplicationException expectedError = ApiErrors.invalidClosingBidTime();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        validator.validate(details);
    }
}
