package com.intuit.cg.backendtechassessment.data.entities;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@Value
@Builder
@Indices({
        @Index(value = "projectId", type = IndexType.NonUnique),
        @Index(value = "minimumBid", type = IndexType.NonUnique),
        @Index(value = "userId", type = IndexType.NonUnique),
})
public class AutoBidEntity implements Serializable {
    @Id
    private final String id;
    private final String userId;
    private final String projectId;
    private final long startingBid;
    private final long minimumBid;
}
