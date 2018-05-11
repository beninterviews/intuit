package com.intuit.cg.backendtechassessment.health;

import com.codahale.metrics.health.HealthCheck;

public class ServiceHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        // Well, we don't have a lot of things to test in here.
        // In real-life we would check for the healthiness of all our dependencies.
        return Result.healthy();
    }
}
