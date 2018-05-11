package com.intuit.cg.backendtechassessment.bids;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Here the decision has been to periodically run a task that will mark the winning bids as being won.
 * The frequency is configurable and is currently set to 1 minute. Is means that once the bidding on a project is over
 * we'll have to wait at most 1 minute before knowing the winning bid.
 * If it doesn't answer the business requirements, another solution could be designed with tighter rules.
 * Assuming we would want to know the winner as soon as the project is over (at a second granularity)
 * a simple solution would be to schedule a task per project.
 * To make that scale with the number of projects we could use a 2 step process. One scheduled task would run periodically (let's say every 5 minutes)
 * and schedule one task per project per distinct second in that 10 minutes period. Worst case scenario we would schedule 60*5=300 tasks.
 * Through code instrumentation, we could fine-tune the frequency (if 300 tasks were too much for example).
 *
 * Also in real-life this wouldn't really work well since we would - most probably - have multiple nodes of that API.
 * We would need to make sure that only one of those nodes handle the processing of the ending projects.
 * Also having that as part of the service is not the best idea, it should probably be a separated project since it doesn't really have
 * any business with the API itself.
 * Now, to make sure that only 1 process (whether it's a stand-alone project or embedded in the API like now) handles that process
 * we could have some kind of leasing mechanism (if our data store was also providing it, that would be perfect).
 * The processes would try to take the lease. First process to take it manages the ending of projects and periodically renews the lease.
 * If the process crashes, the lease will eventually expire and another process will take its place.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EndBidsProcessor implements Managed {
    private final AssessmentConfig config;
    private final ScheduledExecutorService executor;
    private final ProjectManager projectManager;
    private final BidManager bidManager;

    @Override
    public void start() throws Exception {
        log.debug("Starting bids processor");
        final long startNow = 0;
        executor.scheduleAtFixedRate(this::processCompletedProjects, startNow, config.getEndBidFrequency(), config.getEndBidFrequencyUnit());
    }

    @Override
    public void stop() throws Exception {
        log.debug("Stopping bids processor");
        executor.shutdown();

        final long shutdownMillis = 10000L;
        executor.awaitTermination(shutdownMillis, TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    void processCompletedProjects() {
        List<ProjectEntity> completedProjects = projectManager.getCompletedProjects();

        log.debug("{} projects are now completed", completedProjects.size());

        for (ProjectEntity project : completedProjects) {
            BidEntity winningBid = bidManager.getLowestBidForProject(project.getId());

            if (winningBid != null) {
                log.debug("Bid {} won with a value of {}", winningBid.getId(), winningBid.getValue());
                bidManager.markBidAsWon(winningBid);
            }

            log.debug("Project {} is being marked a ended", project.getId());

            projectManager.markProjectAsEnded(project);

            // Marking the bid as won and the project as ended is not atomic (though it could be with our data store)
            // but the operations are idempotent so it's okay.
        }
    }
}
