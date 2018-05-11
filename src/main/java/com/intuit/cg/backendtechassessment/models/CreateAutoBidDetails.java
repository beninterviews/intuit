package com.intuit.cg.backendtechassessment.models;

import lombok.Value;

@Value
public class CreateAutoBidDetails {
    private final Long startingBid;
    private final Long minimumBid;
}
