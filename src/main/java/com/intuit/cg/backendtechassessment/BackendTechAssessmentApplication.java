package com.intuit.cg.backendtechassessment;

import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intuit.cg.backendtechassessment.auth.BasicAuthenticator;
import com.intuit.cg.backendtechassessment.auth.User;
import com.intuit.cg.backendtechassessment.bids.EndBidsProcessor;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.config.AssessmentModule;
import com.intuit.cg.backendtechassessment.constraints.ApiConstraintViolationExceptionMapper;
import com.intuit.cg.backendtechassessment.constraints.GuiceConstraintValidatorFactory;
import com.intuit.cg.backendtechassessment.health.ServiceHealthCheck;
import com.intuit.cg.backendtechassessment.resources.BidsResource;
import com.intuit.cg.backendtechassessment.resources.ProjectsResource;
import com.intuit.cg.backendtechassessment.resources.UsersResource;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * This is entrypoint of the service.
 * Here we configure everything.
 */
@Slf4j
public class BackendTechAssessmentApplication extends Application<AssessmentConfig> {
    @Override
    public void run(AssessmentConfig assessmentConfig, Environment environment) throws Exception {
        Injector injector = Guice.createInjector(new AssessmentModule(assessmentConfig, environment));

        registerEndBidsProcessor(environment, injector);

        registerResources(environment, injector);
        registerAuth(environment, injector);
        registerExceptionMapper(environment);
        registerPrometheusMetrics(environment);
        registerHealthChecks(environment, injector);

        configureValidator(environment, injector);
        configureObjectMapper(environment.getObjectMapper());
    }

    private void registerEndBidsProcessor(Environment environment, Injector injector) {
        environment.lifecycle().manage(injector.getInstance(EndBidsProcessor.class));
    }

    private void registerResources(Environment environment, Injector injector) {
        environment.jersey().register(injector.getInstance(ProjectsResource.class));
        environment.jersey().register(injector.getInstance(BidsResource.class));
        environment.jersey().register(injector.getInstance(UsersResource.class));
    }

    private void registerHealthChecks(Environment environment, Injector injector) {
        log.debug("Registering health checks");
        environment.healthChecks().register("Assessment", injector.getInstance(ServiceHealthCheck.class));
    }

    private void registerAuth(Environment environment, Injector injector) {
        environment.jersey().register(
                new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(injector.getInstance(BasicAuthenticator.class))
                        .buildAuthFilter()));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

    private void registerExceptionMapper(Environment environment) {
        environment.jersey().register(new ApiConstraintViolationExceptionMapper());
    }

    private void registerPrometheusMetrics(Environment environment) {
        DefaultExports.initialize();

        environment.admin()
                .addServlet("prometheusMetrics", new MetricsServlet())
                .addMapping("/prometheusMetrics");
    }

    private void configureValidator(Environment environment, Injector injector) {
        environment.setValidator(
                Validators.newConfiguration()
                        .constraintValidatorFactory(new GuiceConstraintValidatorFactory(injector))
                        .buildValidatorFactory()
                        .getValidator());
    }

    private void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting assessment project");
        new BackendTechAssessmentApplication().run(args);
    }
}
