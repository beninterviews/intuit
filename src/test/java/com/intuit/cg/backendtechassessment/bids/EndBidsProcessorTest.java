package com.intuit.cg.backendtechassessment.bids;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EndBidsProcessorTest {
    @Mock
    private AssessmentConfig config;
    @Mock
    private BidManager bidManager;
    @Mock
    private ProjectManager projectManager;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private EndBidsProcessor endBidsProcessor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        endBidsProcessor = new EndBidsProcessor(config, executorService, projectManager, bidManager);
    }

    @Test
    public void testTaskIsScheduled() throws Exception {
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        endBidsProcessor = new EndBidsProcessor(config, executorService, projectManager, bidManager);

        when(config.getEndBidFrequency()).thenReturn(1L);
        when(config.getEndBidFrequencyUnit()).thenReturn(TimeUnit.SECONDS);

        endBidsProcessor.start();

        verify(executorService).scheduleAtFixedRate(any(), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testMarksBidsAsWinning() {
        ProjectEntity projectEntity = ProjectEntity.builder().id("projectId").build();
        when(projectManager.getCompletedProjects()).thenReturn(ImmutableList.of(projectEntity));
        BidEntity bidEntity = BidEntity.builder().build();
        when(bidManager.getLowestBidForProject("projectId")).thenReturn(bidEntity);

        endBidsProcessor.processCompletedProjects();

        verify(bidManager).markBidAsWon(bidEntity);
        verify(projectManager).markProjectAsEnded(projectEntity);
    }

    @Test
    public void testNotDoingAnythingIfNoWinningBid() {
        ProjectEntity projectEntity = ProjectEntity.builder().id("projectId").build();
        when(projectManager.getCompletedProjects()).thenReturn(ImmutableList.of(projectEntity));
        when(bidManager.getLowestBidForProject("projectId")).thenReturn(null);

        endBidsProcessor.processCompletedProjects();

        // Verity that it's been called once and nothing after that.
        verify(bidManager).getLowestBidForProject("projectId");
        verifyNoMoreInteractions(bidManager);

        verify(projectManager).markProjectAsEnded(projectEntity);
    }

    @Test
    public void testCloseCleansUp() throws Exception {
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        endBidsProcessor = new EndBidsProcessor(config, executorService, projectManager, bidManager);

        endBidsProcessor.stop();

        verify(executorService).shutdown();
        verify(executorService).awaitTermination(10000L, TimeUnit.MILLISECONDS);
    }
}
