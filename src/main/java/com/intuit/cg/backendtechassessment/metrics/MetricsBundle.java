package com.intuit.cg.backendtechassessment.metrics;

import com.google.inject.Singleton;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import java.time.Clock;
import lombok.Getter;

@Singleton
@Getter
public class MetricsBundle {
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    private final CollectorRegistry registry;
    private final Clock clock;

    private final ApiMetricsBundle createProjectMetrics;
    private final ApiMetricsBundle getProjectMetrics;
    private final ApiMetricsBundle postBidMetrics;
    private final ApiMetricsBundle createUserMetrics;

    private final Counter authentication;
    private final Histogram authenticationLatency;

    public MetricsBundle() {
        this(CollectorRegistry.defaultRegistry, Clock.systemUTC());
    }

    public MetricsBundle(CollectorRegistry registry, Clock clock) {
        this.registry = registry;
        this.clock = clock;

        authentication = Counter.build().name("authentication").help("Request Authentication").labelNames("result").register(registry);
        authenticationLatency = Histogram.build().name("authentication_latency").help("Request Authentication Latency").register(registry);

        createProjectMetrics = new ApiMetricsBundle("projects", "create", this);
        getProjectMetrics = new ApiMetricsBundle("projects", "get", this);
        postBidMetrics = new ApiMetricsBundle("bids", "post", this);
        createUserMetrics = new ApiMetricsBundle("users", "create", this);
    }
}
