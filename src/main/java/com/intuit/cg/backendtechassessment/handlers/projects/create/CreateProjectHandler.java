package com.intuit.cg.backendtechassessment.handlers.projects.create;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import com.intuit.cg.backendtechassessment.models.CreateProjectDetails;
import com.intuit.cg.backendtechassessment.models.Project;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CreateProjectHandler {
    private final ProjectManager projectManager;

    public Project handleRequest(CreateProjectDetails details, User user) {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .name(details.getName())
                        .description(details.getDescription())
                        .budget(details.getBudget())
                        .closingBidTime(details.getClosingBidTime().toEpochSecond())
                        .userId(user.getId())
                        .build();

        projectManager.insertProject(projectEntity);

        log.debug("Project {} has been created", projectEntity);

        return Project.fromEntity(projectEntity);
    }
}
