package com.intuit.cg.backendtechassessment.config;

import io.dropwizard.Configuration;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AssessmentConfig extends Configuration {
    private boolean authDisabled;

    private long endBidFrequency;
    private TimeUnit endBidFrequencyUnit;

    private long bidStepBudgetPercent;

    private String dataStorePath;
}
