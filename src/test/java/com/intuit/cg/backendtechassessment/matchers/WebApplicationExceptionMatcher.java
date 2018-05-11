package com.intuit.cg.backendtechassessment.matchers;

import javax.ws.rs.WebApplicationException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class WebApplicationExceptionMatcher extends TypeSafeMatcher<WebApplicationException> {
    private final int code;
    private final String message;

    public WebApplicationExceptionMatcher(WebApplicationException expected) {
        this.code = expected.getResponse().getStatus();
        this.message = expected.getMessage();
    }

    @Override
    protected boolean matchesSafely(WebApplicationException item) {
        return item.getResponse().getStatus() == code &&
                (message == null || item.getMessage().equals(message));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A WebApplicationException with an error code of ").appendText(Integer.toString(code));
    }
}
