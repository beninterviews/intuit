package com.intuit.cg.backendtechassessment.data.managers;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.lte;

import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.data.InMemoryDataStore;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProjectManager {
    private final InMemoryDataStore dataStore;

    public ProjectEntity getProject(String id) {
        return dataStore.getProjects().find(eq("id", id)).firstOrDefault();
    }

    public void insertProject(ProjectEntity projectEntity) {
        dataStore.getProjects().insert(projectEntity);
    }

    public List<ProjectEntity> getCompletedProjects() {
        return dataStore.getProjects()
                .find(
                        and(
                                eq("ended", false),
                                lte("closingBidTime", OffsetDateTime.now().toEpochSecond())
                        ))
                .toList();
    }

    public void markProjectAsEnded(ProjectEntity projectEntity) {
        ProjectEntity updatedEntity =
                projectEntity.toBuilder()
                        .ended(true)
                        .build();

        dataStore.getProjects().update(updatedEntity);
    }
}
