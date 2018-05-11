package com.intuit.cg.backendtechassessment.models;

import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import lombok.Value;

@Value
public class Bid {
    private final String id;
    private final String userId;
    private final long value;

    public static Bid fromEntity(BidEntity bidEntity) {
        return new Bid(bidEntity.getId(),
                bidEntity.getUserId(),
                bidEntity.getValue());
    }
}
