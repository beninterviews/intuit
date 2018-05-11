package com.intuit.cg.backendtechassessment.models;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class CreateProjectDetails {
    private final String name;
    private final String description;
    private final Long budget;
    private final OffsetDateTime closingBidTime;
}
