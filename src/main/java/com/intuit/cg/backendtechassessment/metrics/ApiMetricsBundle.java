package com.intuit.cg.backendtechassessment.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ApiMetricsBundle {
    private static final String REQUESTS_SUFFIX = "requests";
    private static final String OVERALL_LATENCY_SUFFIX = "latency_overall";

    protected final String prefix;
    protected final MetricsBundle bundle;

    private final Counter requests;
    private final Histogram overallLatency;

    ApiMetricsBundle(@NonNull String resource, @NonNull String method, MetricsBundle bundle) {
        this.prefix = resource + "_" + method + "_";
        this.bundle = bundle;

        requests = Counter.build()
                .name(prefix + REQUESTS_SUFFIX)
                .help(prefix + REQUESTS_SUFFIX)
                .labelNames("result") // we label per HTTP result code.
                .register(bundle.getRegistry());

        overallLatency = Histogram.build()
                .name(prefix + OVERALL_LATENCY_SUFFIX)
                .help(prefix + OVERALL_LATENCY_SUFFIX)
                .labelNames("result") // we label per HTTP result code.
                .register(bundle.getRegistry());
    }
}