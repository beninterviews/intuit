package com.intuit.cg.backendtechassessment.utils;

import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BidStepCalculator {
    private final AssessmentConfig config;
    private final BidManager bidManager;

    public long calculateMinimumBid(ProjectEntity projectEntity) {
        BidEntity lowestBid = bidManager.getLowestBidForProject(projectEntity.getId());
        if (lowestBid != null) {
            // One symbolic dollar!
            return Math.max(1, lowestBid.getValue() - calculateBidStep(projectEntity));
        } else {
            return projectEntity.getBudget();
        }
    }

    public long calculateBidStep(ProjectEntity projectEntity) {
        return projectEntity.getBudget() * config.getBidStepBudgetPercent() / 100;
    }
}
