package com.intuit.cg.backendtechassessment.handlers.projects.get;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.Project;
import com.intuit.cg.backendtechassessment.utils.BidStepCalculator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GetProjectHandler {
    private final ProjectManager projectManager;
    private final BidManager bidManager;
    private final BidStepCalculator bidStepCalculator;

    public Project handleRequest(String id) {
        return Optional.ofNullable(projectManager.getProject(id))
                .map(project -> Project.fromEntity(project, bidStepCalculator.calculateMinimumBid(project)))
                .orElseThrow(() -> ApiErrors.unknownProject(id));
    }
}
