package com.intuit.cg.backendtechassessment.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.Constants;
import com.intuit.cg.backendtechassessment.async.AsyncResponder;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.constraints.ApiConstraint;
import com.intuit.cg.backendtechassessment.handlers.projects.create.CreateProjectHandler;
import com.intuit.cg.backendtechassessment.handlers.projects.get.GetProjectHandler;
import com.intuit.cg.backendtechassessment.metrics.MetricsBundle;
import com.intuit.cg.backendtechassessment.models.CreateProjectDetails;
import com.intuit.cg.backendtechassessment.models.Project;
import com.intuit.cg.backendtechassessment.validators.CreateProjectValidator;
import io.dropwizard.auth.Auth;
import javax.ws.rs.GET;
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
public class ProjectsResource {
    private final MetricsBundle metricsBundle;
    private final CreateProjectHandler createProjectHandler;
    private final GetProjectHandler getProjectHandler;

    @POST
    @Path("/projects")
    public void createProjectAsync(
            @Suspended final AsyncResponse asyncResponse,
            @Auth User user,
            @ApiConstraint(CreateProjectValidator.class) CreateProjectDetails createProjectDetails) {

        AsyncResponder<Project> asyncResponder = new AsyncResponder<>(asyncResponse, metricsBundle.getCreateProjectMetrics());

        asyncResponder.handleResult(() -> createProjectHandler.handleRequest(createProjectDetails, user));
    }

    @GET
    @Path("/projects/{projectId}")
    public void getProjectAsync(
            @Suspended final AsyncResponse asyncResponse,
            @Auth User user,
            @PathParam("projectId") String projectId) {

        AsyncResponder<Project> asyncResponder = new AsyncResponder<>(asyncResponse, metricsBundle.getGetProjectMetrics());

        asyncResponder.handleResult(() -> getProjectHandler.handleRequest(projectId));
    }
}
