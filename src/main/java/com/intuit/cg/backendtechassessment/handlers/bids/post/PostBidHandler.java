package com.intuit.cg.backendtechassessment.handlers.bids.post;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.data.entities.AutoBidEntity;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.PostBidDetails;
import com.intuit.cg.backendtechassessment.utils.BidStepCalculator;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PostBidHandler {
    private final ProjectManager projectManager;
    private final BidManager bidManager;
    private final BidStepCalculator bidStepCalculator;

    public Bid handleRequest(PostBidDetails details, String projectId, User user) {
        ProjectEntity projectEntity = Optional.ofNullable(projectManager.getProject(projectId))
                .orElseThrow(() -> ApiErrors.unknownProject(projectId));

        validateRequest(projectEntity, details);

        BidEntity bidEntity =
                BidEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(user.getId())
                        .projectId(projectId)
                        .value(details.getValue())
                        .build();

        bidManager.insertBid(bidEntity);

        Bid createdBid = Bid.fromEntity(bidEntity);
        // We've created the bid for the current user but we also need to check if there's some auto-bid
        generateBidsForAutoBids(projectEntity, createdBid);

        return createdBid;
    }

    private void validateRequest(ProjectEntity projectEntity, PostBidDetails details) {
        // We don't allow big on a project that's over
        // This doesn't really take into account possible clock drift, but I believe that it is okay for such a service.
        // I do not think that people would wait the last second to bid on a project.
        if (projectEntity.getClosingBidTime() <= OffsetDateTime.now().toEpochSecond()) {
            throw ApiErrors.invalidBid();
        }

        // We don't allow a bid over the budget.
        if (details.getValue() > projectEntity.getBudget()) {
            throw ApiErrors.invalidBid();
        }

        // We don't allow bids higher than the current lowest bid.
        if (details.getValue() > bidStepCalculator.calculateMinimumBid(projectEntity)) {
            // TODO More detailed error message?
            throw ApiErrors.invalidBid();
        }

        // In a real project we might want to allow for bids higher than the current lowest bid.
        // Why? A seller might want to choose a bid higher than the lowest if the buyer has better reviews for example.
        // Right now, we select the lowest bid and mark is as "winning" when the bidding ends, but it should be smarter.
    }

    private void generateBidsForAutoBids(ProjectEntity projectEntity, Bid currentBid) {
        // This is a really naive implementation of the auto-bid feature.
        // It will generate a new bid for all auto-bids with a minimum bid lower than the created bid.
        // Order of the auto-bids will matter with the current implementation (read code to see why).
        // By analyzing the behavior of the application, someone could figure out how to win auto-bid ties every time.
        // With more business requirements that feature could be fine-tuned (for example, what happens if a user posts a bid while it also has an auto-bid?).
        // Also the way it's currently implemented will impede the postBid latency for the user who posted the original bid.
        // If there's a large number of auto-bid in place, the duration of the call could fall below our SLA.
        // A different component should probably be created for that and using some kind of queueing mechanism to schedule auto-bids.
        // If the auto-bid feature is moved out of that codepath and into something of async nature (like a queuing mechanism)
        // we would need to make sure that all auto-bids are posted before choosing the winning bid.
        long targetBid = currentBid.getValue();
        String currentUserId = currentBid.getUserId();
        List<AutoBidEntity> autoBids;

        long bidStep = bidStepCalculator.calculateBidStep(projectEntity);

        do {
            autoBids = bidManager.getAutoBidsForProject(projectEntity.getId(), targetBid, currentUserId);

            log.debug("{} auto-bids exist for project {}", autoBids.size(), projectEntity.getId());

            for (AutoBidEntity autoBid : autoBids) {
                long newBid = Math.max(targetBid - bidStep, autoBid.getMinimumBid());

                if (newBid < targetBid) {
                    BidEntity bidEntity =
                            BidEntity.builder()
                                    .id(UUID.randomUUID().toString())
                                    .userId(autoBid.getUserId())
                                    .projectId(projectEntity.getId())
                                    .value(newBid)
                                    .build();

                    log.debug("Creating new bid {}", bidEntity);

                    bidManager.insertBid(bidEntity);

                    currentUserId = autoBid.getUserId();
                    targetBid = newBid;
                }
            }
        } while (!autoBids.isEmpty());
    }
}
