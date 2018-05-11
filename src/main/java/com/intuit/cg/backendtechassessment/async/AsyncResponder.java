package com.intuit.cg.backendtechassessment.async;

import com.intuit.cg.backendtechassessment.exceptions.WebApplicationExceptions;
import com.intuit.cg.backendtechassessment.metrics.ApiMetricsBundle;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * This provides a nice way to handle all API responses.
 * It manages metrics, errors and successes.
 * We don't really have any asynchronous code path in the current code but any project will eventually grow
 * and will need asynchronous operations, hence the usage of the AsyncResponse.
 */
@Slf4j
public class AsyncResponder<T> {
    private static final int TIMEOUT_IN_SECONDS = 10;

    private final AsyncResponse asyncResponse;
    private final ApiMetricsBundle metrics;

    private final long requestStartTimeMs;

    public AsyncResponder(AsyncResponse asyncResponse, ApiMetricsBundle metrics) {
        this.asyncResponse = asyncResponse;
        this.metrics = metrics;

        this.requestStartTimeMs = metrics.getBundle().getClock().instant().toEpochMilli();

        setTimeout();
    }

    private void setTimeout() {
        // Prevents timeout when running debugging, just use -DranWithIde=true in your JVM parameters.
        if (!Boolean.getBoolean("ranWithIde")) {
            asyncResponse.setTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            asyncResponse.setTimeoutHandler(asyncResponse -> error(Response.Status.GATEWAY_TIMEOUT));
        }
    }

    public void handleResult(Supplier<T> resultProvider) {
        try {
            resume(resultProvider.get());
        } catch (Exception exc) {
            error(exc);
        }
    }

    void resume(T entity) {
        resume(entity, Response.Status.OK);
    }

    void resume(T entity, Response.Status status) {
        resume(Optional.of(entity), status);
    }

    private void resume(Optional<T> entity, Response.Status status) {
        emitMetrics(status);

        log.trace("Returning status {}", status);

        Response.ResponseBuilder responseBuilder = Response.status(status);

        if (entity.isPresent()) {
            responseBuilder = responseBuilder.entity(entity.get());
        }

        boolean resumed = asyncResponse.resume(responseBuilder.build());
        if (!resumed) {
            log.warn("Attempted to complete a request but a response has already been generated");
        }
    }

    private void error(Response.Status status) {
        emitMetrics(status);

        log.warn("Returning error status {}", status);

        boolean resumed = asyncResponse.resume(Response.status(status).build());
        if (!resumed) {
            log.warn("Attempted to complete a request but a response has already been generated");
        }
    }

    void error(Throwable throwable) {
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof WebApplicationException) {
            handleWebApplicationException((WebApplicationException) throwable);
        } else {
            handleWebApplicationException(WebApplicationExceptions.internalError());
        }
    }

    private void handleWebApplicationException(WebApplicationException exception) {
        // Log level is dependent on severity
        if (exception.getResponse().getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
            log.error("A server error exception is being returned", exception);
        } else {
            log.debug("A non server error exception is being returned", exception);
        }

        emitMetrics(exception.getResponse().getStatusInfo());

        asyncResponse.resume(exception);
    }

    private void emitMetrics(Response.StatusType status) {
        double elapsedTimeSeconds = (metrics.getBundle().getClock().instant().toEpochMilli() - requestStartTimeMs) / 1000.0;

        String label = Integer.toString(status.getStatusCode());
        metrics.getOverallLatency().labels(label).observe(elapsedTimeSeconds);
        metrics.getRequests().labels(label).inc();
    }
}
