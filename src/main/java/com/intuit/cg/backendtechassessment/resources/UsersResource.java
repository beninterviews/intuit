package com.intuit.cg.backendtechassessment.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.Constants;
import com.intuit.cg.backendtechassessment.async.AsyncResponder;
import com.intuit.cg.backendtechassessment.constraints.ApiConstraint;
import com.intuit.cg.backendtechassessment.handlers.users.create.CreateUserHandler;
import com.intuit.cg.backendtechassessment.metrics.MetricsBundle;
import com.intuit.cg.backendtechassessment.models.CreateUserDetails;
import com.intuit.cg.backendtechassessment.models.User;
import com.intuit.cg.backendtechassessment.validators.CreateUserValidator;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@Path(Constants.BASE_PATH)
@Produces({"application/json"})
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UsersResource {
    private final MetricsBundle metricsBundle;
    private final CreateUserHandler createUserHandler;

    @POST
    @Path("/users")
    public void createUserAsync(
            @Suspended final AsyncResponse asyncResponse,
            @ApiConstraint(CreateUserValidator.class) CreateUserDetails createUserDetails) {

        AsyncResponder<User> asyncResponder = new AsyncResponder<>(asyncResponse, metricsBundle.getCreateUserMetrics());

        asyncResponder.handleResult(() -> createUserHandler.handleRequest(createUserDetails));
    }
}
