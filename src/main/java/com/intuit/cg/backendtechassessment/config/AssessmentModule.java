package com.intuit.cg.backendtechassessment.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.bids.EndBidsProcessor;
import com.intuit.cg.backendtechassessment.data.managers.BidManager;
import com.intuit.cg.backendtechassessment.data.managers.ProjectManager;
import io.dropwizard.setup.Environment;
import java.util.concurrent.ScheduledExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AssessmentModule extends AbstractModule {
    private final AssessmentConfig config;
    private final Environment environment;

    @Override
    protected void configure() {
        bind(AssessmentConfig.class).toInstance(config);
    }

    @Provides
    @Singleton
    public EndBidsProcessor endBidsProcessor(ProjectManager projectManager, BidManager bidManager) {
        ScheduledExecutorService scheduledExecutorService = environment.lifecycle().scheduledExecutorService("endBidsProcessor-%d").build();
        return new EndBidsProcessor(config, scheduledExecutorService, projectManager, bidManager);
    }
}
