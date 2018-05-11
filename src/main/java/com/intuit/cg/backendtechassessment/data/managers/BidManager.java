package com.intuit.cg.backendtechassessment.data.managers;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.lte;
import static org.dizitart.no2.objects.filters.ObjectFilters.not;

import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.data.InMemoryDataStore;
import com.intuit.cg.backendtechassessment.data.entities.AutoBidEntity;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BidManager {
    private final InMemoryDataStore dataStore;

    public void insertBid(BidEntity bidEntity) {
        dataStore.getBids().insert(bidEntity);
    }

    public List<BidEntity> getBidsPerProject(String projectId) {
        return dataStore.getBids().find(eq("projectId", projectId)).toList();
    }

    public BidEntity getLowestBidForProject(String projectId) {
        return StreamSupport.stream(dataStore.getBids().find(eq("projectId", projectId)).spliterator(), false)
                .sorted(Comparator.comparingLong(BidEntity::getValue))
                .findFirst()
                .orElse(null);
    }

    public void markBidAsWon(BidEntity bidEntity) {
        BidEntity updatedEntity =
                bidEntity.toBuilder()
                        .winning(true)
                        .build();

        dataStore.getBids().update(updatedEntity);
    }

    public void insertAutoBid(AutoBidEntity autoBidEntity) {
        dataStore.getAutoBids().insert(autoBidEntity);
    }

    public List<AutoBidEntity> getAutoBidsForProject(String projectId, long currentBid, String userId) {
        return dataStore.getAutoBids()
                .find(
                        and(
                                eq("projectId", projectId),
                                lte("minimumBid", currentBid),
                                not(eq("userId", userId))
                        ),
                        FindOptions.sort("minimumBid", SortOrder.Descending))
                .toList();
    }
}
