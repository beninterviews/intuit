package com.intuit.cg.backendtechassessment.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.Constants;
import com.intuit.cg.backendtechassessment.async.AsyncResponder;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.constraints.ApiConstraint;
import com.intuit.cg.backendtechassessment.handlers.bids.create.CreateAutoBidHandler;
import com.intuit.cg.backendtechassessment.handlers.bids.post.PostBidHandler;
import com.intuit.cg.backendtechassessment.metrics.MetricsBundle;
import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.CreateAutoBidDetails;
import com.intuit.cg.backendtechassessment.models.PostBidDetails;
import com.intuit.cg.backendtechassessment.validators.CreateAutoBidValidator;
import com.intuit.cg.backendtechassessment.validators.PostBidValidator;
import io.dropwizard.auth.Auth;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
public class BidsResource {
    private final MetricsBundle metricsBundle;
    private final PostBidHandler postBidHandler;
    private final CreateAutoBidHandler createAutoBidHandler;

    @POST
    @Path("/projects/{projectId}/bids")
    public void postBidAsync(
            @Suspended final AsyncResponse asyncResponse,
            @Auth User user,
            @ApiConstraint(PostBidValidator.class) PostBidDetails postBidDetails,
            @PathParam("projectId") String projectId) {

        AsyncResponder<Bid> asyncResponder = new AsyncResponder<>(asyncResponse, metricsBundle.getPostBidMetrics());

        asyncResponder.handleResult(() -> postBidHandler.handleRequest(postBidDetails, projectId, user));
    }

    @POST
    @Path("/projects/{projectId}/autobid")
    public void createAutoBidAsync(
            @Suspended final AsyncResponse asyncResponse,
            @Auth User user,
            @ApiConstraint(CreateAutoBidValidator.class) CreateAutoBidDetails createAutoBidDetails,
            @PathParam("projectId") String projectId) {

        AsyncResponder<CreateAutoBidDetails> asyncResponder = new AsyncResponder<>(asyncResponse, metricsBundle.getPostBidMetrics());

        asyncResponder.handleResult(() -> createAutoBidHandler.handleRequest(createAutoBidDetails, projectId, user));
    }
}
