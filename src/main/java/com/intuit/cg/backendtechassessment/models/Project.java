package com.intuit.cg.backendtechassessment.models;

import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Value;

@Value
public class Project {
    private final String id;
    private final String userId;
    private final String name;
    private final String description;
    private final long budget;
    private final OffsetDateTime closingBidTime;
    private final long minimumBid;

    public static Project fromEntity(ProjectEntity projectEntity) {
        return new Project(projectEntity.getId(),
                projectEntity.getUserId(),
                projectEntity.getName(),
                projectEntity.getDescription(),
                projectEntity.getBudget(),
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(projectEntity.getClosingBidTime()), ZoneOffset.UTC),
                projectEntity.getBudget());
    }

    public static Project fromEntity(ProjectEntity projectEntity, long minimumBid) {
        return new Project(projectEntity.getId(),
                projectEntity.getUserId(),
                projectEntity.getName(),
                projectEntity.getDescription(),
                projectEntity.getBudget(),
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(projectEntity.getClosingBidTime()), ZoneOffset.UTC),
                minimumBid);
    }
}
