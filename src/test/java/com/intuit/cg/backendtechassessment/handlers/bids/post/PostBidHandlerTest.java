package com.intuit.cg.backendtechassessment.handlers.bids.post;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.data.entities.AutoBidEntity;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.PostBidDetails;
import com.intuit.cg.backendtechassessment.utils.BidStepCalculator;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PostBidHandlerTest {
    @Mock
    private BidStepCalculator bidStepCalculator;
    @Mock
    private BidManager bidManager;
    @Mock
    private ProjectManager projectManager;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final PostBidDetails details = new PostBidDetails(950L);
    private final String projectId = "projectId";
    private final User user = new User("userId", "name");

    private PostBidHandler postBidHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(bidStepCalculator.calculateMinimumBid(any())).thenReturn(950L);
        when(bidStepCalculator.calculateBidStep(any())).thenReturn(100L);

        postBidHandler = new PostBidHandler(projectManager, bidManager, bidStepCalculator);
    }

    @Test
    public void testSuccess() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1000L)
                        .closingBidTime(OffsetDateTime.now().plusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        when(bidManager.getLowestBidForProject(projectId)).thenReturn(null);

        postBidHandler.handleRequest(details, projectId, user);

        ArgumentCaptor<BidEntity> captor = ArgumentCaptor.forClass(BidEntity.class);
        verify(bidManager).insertBid(captor.capture());

        // Make sure we create a bid with the proper data.
        BidEntity bidEntity = captor.getValue();
        assertThat(bidEntity.getProjectId(), equalTo(projectId));
        assertThat(bidEntity.getUserId(), equalTo(user.getId()));
        assertThat(bidEntity.getValue(), equalTo(details.getValue()));
        assertThat(bidEntity.isWinning(), is(false));
    }

    @Test
    public void testBidOnNonExistingProject() {
        when(projectManager.getProject(projectId)).thenReturn(null);

        WebApplicationException expectedError = ApiErrors.unknownProject(projectId);
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        postBidHandler.handleRequest(details, projectId, user);
    }

    @Test
    public void testBidOnClosedProject() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1000L)
                        .closingBidTime(OffsetDateTime.now().minusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        postBidHandler.handleRequest(details, projectId, user);
    }

    @Test
    public void testBidMoreThanBudget() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1L)
                        .closingBidTime(OffsetDateTime.now().plusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        postBidHandler.handleRequest(details, projectId, user);
    }

    @Test
    public void testBidHigherThanLowest() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1000L)
                        .closingBidTime(OffsetDateTime.now().plusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        when(bidStepCalculator.calculateMinimumBid(projectEntity)).thenReturn(1L);

        WebApplicationException expectedError = ApiErrors.invalidBid();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        postBidHandler.handleRequest(details, projectId, user);
    }

    @Test
    public void testAutoBidsAreTriggered() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1000L)
                        .closingBidTime(OffsetDateTime.now().plusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        when(bidManager.getLowestBidForProject(projectId)).thenReturn(null);

        AutoBidEntity autoBid1 = AutoBidEntity.builder().minimumBid(500L).userId("u1").build();
        AutoBidEntity autoBid2 = AutoBidEntity.builder().minimumBid(200L).userId("u2").build();
        List<AutoBidEntity> autoBids = ImmutableList.of(autoBid1, autoBid2);

        mockAutoBids(autoBids);

        postBidHandler.handleRequest(details, projectId, user);

        // Make sure the bids were generated.
        // Here's the breakdown:
        // 2 auto-bids with a minimum of $500 and $200.
        // The current bid starts at $950.
        // A auto-bid step of $100.
        // It's going to generate the bids - 850 (u1), 750 (u2), 650 (u1), 550 (u2), 500 (u1) (stop of auto-bid 1), 400 (u2)
        int expectedBids = 6 + 1; // Add one because of the original bid

        ArgumentCaptor<BidEntity> captor = ArgumentCaptor.forClass(BidEntity.class);
        verify(bidManager, times(expectedBids)).insertBid(captor.capture());

        ImmutableList<Pair<Long, String>> expectedBidsValues =
                ImmutableList.<Pair<Long, String>>builder()
                        .add(new Pair<>(850L, "u1"))
                        .add(new Pair<>(750L, "u2"))
                        .add(new Pair<>(650L, "u1"))
                        .add(new Pair<>(550L, "u2"))
                        .add(new Pair<>(500L, "u1"))
                        .add(new Pair<>(400L, "u2"))
                        .build();

        // Don't assert the first bid, it's been verified in the "testSuccess" test.
        for (int i = 1; i < expectedBids; i++) {
            assertThat(captor.getAllValues().get(i).getValue(), equalTo(expectedBidsValues.get(i - 1).getKey()));
            assertThat(captor.getAllValues().get(i).getUserId(), equalTo(expectedBidsValues.get(i - 1).getValue()));
        }
    }

    @Test
    public void testAutoBidsTie() {
        ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .budget(1000L)
                        .closingBidTime(OffsetDateTime.now().plusDays(1).toEpochSecond())
                        .build();

        when(projectManager.getProject(projectId)).thenReturn(projectEntity);

        when(bidManager.getLowestBidForProject(projectId)).thenReturn(null);

        AutoBidEntity autoBid1 = AutoBidEntity.builder().minimumBid(500L).userId("u1").build();
        AutoBidEntity autoBid2 = AutoBidEntity.builder().minimumBid(500L).userId("u2").build();
        List<AutoBidEntity> autoBids = ImmutableList.of(autoBid1, autoBid2);

        mockAutoBids(autoBids);

        postBidHandler.handleRequest(details, projectId, user);

        // Make sure the bids were generated.
        // Here's the breakdown:
        // 2 auto-bids with a minimum of $500 and $200.
        // The current bid starts at $950.
        // A auto-bid step of $100.
        // It's going to generate the bids - 850 (u1), 750 (u2), 650 (u1), 550 (u2), 500 (u1) (stop of auto-bid 1)
        // In that case u1 will win even though u2 had the same minimum limit.
        int expectedBids = 5 + 1; // Add one because of the original bid

        ArgumentCaptor<BidEntity> captor = ArgumentCaptor.forClass(BidEntity.class);
        verify(bidManager, times(expectedBids)).insertBid(captor.capture());

        captor.getAllValues().forEach(System.out::println);

        ImmutableList<Pair<Long, String>> expectedBidsValues =
                ImmutableList.<Pair<Long, String>>builder()
                        .add(new Pair<>(850L, "u1"))
                        .add(new Pair<>(750L, "u2"))
                        .add(new Pair<>(650L, "u1"))
                        .add(new Pair<>(550L, "u2"))
                        .add(new Pair<>(500L, "u1"))
                        .build();

        // Don't assert the first bid, it's been verified in the "testSuccess" test.
        for (int i = 1; i < expectedBids; i++) {
            assertThat(captor.getAllValues().get(i).getValue(), equalTo(expectedBidsValues.get(i - 1).getKey()));
            assertThat(captor.getAllValues().get(i).getUserId(), equalTo(expectedBidsValues.get(i - 1).getValue()));
        }
    }

    private void mockAutoBids(List<AutoBidEntity> autoBids) {
        when(bidManager.getAutoBidsForProject(eq(projectId), anyLong(), anyString())).thenAnswer(
                invocation -> {
                    // We still have to make that mock a bit smart and follow the
                    // supposed logic of the method (returning auto-bids <= supplied value ordered bu minimum bid desc)
                    // and userId different from supplied.
                    Long value = invocation.getArgument(1);
                    String userId = invocation.getArgument(2);
                    return autoBids.stream()
                            .filter(bid -> bid.getMinimumBid() < value && !bid.getUserId().equalsIgnoreCase(userId))
                            .sorted(Comparator.comparing(AutoBidEntity::getMinimumBid).reversed())
                            .collect(Collectors.toList());
                });
    }
}
