package com.intuit.cg.backendtechassessment.handlers.bids.create;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.data.entities.AutoBidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.CreateAutoBidDetails;
import com.intuit.cg.backendtechassessment.utils.BidStepCalculator;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CreateAutoBidHandler {
    private final ProjectManager projectManager;
    private final BidManager bidManager;
    private final BidStepCalculator bidStepCalculator;

    public CreateAutoBidDetails handleRequest(CreateAutoBidDetails details, String projectId, User user) {
        ProjectEntity projectEntity = Optional.ofNullable(projectManager.getProject(projectId))
                .orElseThrow(() -> ApiErrors.unknownProject(projectId));

        validateRequest(projectEntity, details);

        AutoBidEntity autoBidEntity =
                AutoBidEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(user.getId())
                        .projectId(projectId)
                        .minimumBid(details.getMinimumBid())
                        .startingBid(details.getStartingBid())
                        .build();

        bidManager.insertAutoBid(autoBidEntity);

        return details;
    }

    private void validateRequest(ProjectEntity projectEntity, CreateAutoBidDetails details) {
        // We don't allow big on a project that's over
        // This doesn't really take into account possible clock drift, but I believe that it is okay for such a service.
        // I do not think that people would wait the last second to bid on a project.
        if (projectEntity.getClosingBidTime() <= OffsetDateTime.now().toEpochSecond()) {
            throw ApiErrors.invalidBid();
        }

        // We don't allow a bid over the budget.
        if (details.getStartingBid() > projectEntity.getBudget()) {
            throw ApiErrors.invalidBid();
        }

        // We don't allow bids higher than the current lowest bid.
        if (details.getStartingBid() > bidStepCalculator.calculateMinimumBid(projectEntity)) {
            throw ApiErrors.invalidBid();
        }

        // In a real project we might want to allow for bids higher than the current lowest bid.
        // Why? A seller might want to choose a bid higher than the lowest if the buyer has better reviews for example.
        // Right now, we select the lowest bid and mark is as "winning" when the bidding ends, but it should be smarter.

        // TODO We can probably refactor that since this code is pretty similar to the one in the PostBidHandler.
    }
}
