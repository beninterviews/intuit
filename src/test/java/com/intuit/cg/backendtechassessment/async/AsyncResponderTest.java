package com.intuit.cg.backendtechassessment.async;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intuit.cg.backendtechassessment.metrics.ApiMetricsBundle;
import com.intuit.cg.backendtechassessment.metrics.MetricsBundle;
import io.prometheus.client.CollectorRegistry;
import java.time.Clock;
import java.time.Instant;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AsyncResponderTest {
    @Mock
    private AsyncResponse asyncResponse;
    @Mock
    private Clock clock;

    private CollectorRegistry metricsRegistry = new CollectorRegistry();
    private ApiMetricsBundle apiMetricsBundle;

    private final long durationMs = 2323L;
    private final double durationSeconds = durationMs / 1000.0;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        MetricsBundle metricsBundle = new MetricsBundle(metricsRegistry, clock);
        // Get any of the metrics bundle, it doesn't really matter.
        apiMetricsBundle = metricsBundle.getCreateProjectMetrics();

        long beginTime = 2342345L;
        long endTime = beginTime + durationMs;

        when(clock.instant()).thenReturn(Instant.ofEpochMilli(beginTime), Instant.ofEpochMilli(endTime));
    }

    @Test
    public void testResumeOK() {
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.resume("test");

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponse).resume(argumentCaptor.capture());
        
        final Response response = argumentCaptor.getValue();
        assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo("test"));

        // Validate metrics
        Double successfulRequestCount = getMetricValueForCode("projects_create_requests", Status.OK.getStatusCode());
        Double getRequestLatencyCount = getMetricValueForCode("projects_create_latency_overall_count", Status.OK.getStatusCode());
        Double getRequestLatency = getMetricValueForCode("projects_create_latency_overall_sum", Status.OK.getStatusCode());

        assertThat(successfulRequestCount, equalTo(1.0));
        assertThat(getRequestLatencyCount, equalTo(1.0));
        assertThat(getRequestLatency, equalTo(durationSeconds));
    }

    @Test
    public void testResumeWithStatus() {
        Response.Status created = Response.Status.CREATED;
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.resume("test", created);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final Response response = argumentCaptor.getValue();
        assertThat(response.getStatus(), equalTo(created.getStatusCode()));
        assertThat(response.getEntity(), equalTo("test"));

        // Validate metrics
        Double successfulRequestCount = getMetricValueForCode("projects_create_requests", created.getStatusCode());
        Double getRequestLatencyCount = getMetricValueForCode("projects_create_latency_overall_count", created.getStatusCode());
        Double getRequestLatency = getMetricValueForCode("projects_create_latency_overall_sum", created.getStatusCode());

        assertThat(successfulRequestCount, equalTo(1.0));
        assertThat(getRequestLatencyCount, equalTo(1.0));
        assertThat(getRequestLatency, equalTo(durationSeconds));
    }

    @Test
    public void testResumeWithFailedStatus() {
        Response.Status unauthorized = Response.Status.UNAUTHORIZED;
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.resume("test", unauthorized);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final Response response = argumentCaptor.getValue();
        assertThat(response.getStatus(), equalTo(unauthorized.getStatusCode()));
        assertThat(response.getEntity(), equalTo("test"));

        // Validate metrics
        Double unauthorizedRequestCount = getMetricValueForCode("projects_create_requests", unauthorized.getStatusCode());
        Double getRequestLatencyCount = getMetricValueForCode("projects_create_latency_overall_count", unauthorized.getStatusCode());
        Double getRequestLatency = getMetricValueForCode("projects_create_latency_overall_sum", unauthorized.getStatusCode());

        assertThat(unauthorizedRequestCount, equalTo(1.0));
        assertThat(getRequestLatencyCount, equalTo(1.0));
        assertThat(getRequestLatency, equalTo(durationSeconds));
    }

    @Test
    public void testResumeWithFailedServerStatus() {
        Status serverError = Status.INTERNAL_SERVER_ERROR;
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.resume("test", serverError);

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final Response response = argumentCaptor.getValue();
        assertThat(response.getStatus(), equalTo(serverError.getStatusCode()));
        assertThat(response.getEntity(), equalTo("test"));

        // Validate metrics
        Double serverErrorCount = getMetricValueForCode("projects_create_requests", serverError.getStatusCode());
        assertThat(serverErrorCount, equalTo(1.0));
    }

    @Test
    public void testRegularExceptionAreTransformedAsInternalError() {
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.error(new RuntimeException("test"));

        ArgumentCaptor<WebApplicationException> argumentCaptor = ArgumentCaptor.forClass(WebApplicationException.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final WebApplicationException exception = argumentCaptor.getValue();
        assertThat(exception.getResponse().getStatus(), equalTo(500));

        // Validate metrics
        Double internalErrorCount = getMetricValueForCode("projects_create_requests", 500);
        assertThat(internalErrorCount, equalTo(1.0));
    }

    @Test
    public void testWebApplicationExceptionStayTheSame() {
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.error(new WebApplicationException("test", 400));

        ArgumentCaptor<WebApplicationException> argumentCaptor = ArgumentCaptor.forClass(WebApplicationException.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final WebApplicationException exception = argumentCaptor.getValue();
        assertThat(exception.getResponse().getStatus(), equalTo(400));

        // Validate metrics
        Double internalErrorCount = getMetricValueForCode("projects_create_requests", 400);
        assertThat(internalErrorCount, equalTo(1.0));
    }

    @Test
    public void testServerErrorEmitFailure() {
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.error(new WebApplicationException("test", 500));

        ArgumentCaptor<WebApplicationException> argumentCaptor = ArgumentCaptor.forClass(WebApplicationException.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final WebApplicationException exception = argumentCaptor.getValue();
        assertThat(exception.getResponse().getStatus(), equalTo(500));

        // Validate metrics
        Double internalErrorCount = getMetricValueForCode("projects_create_requests", 500);
        assertThat(internalErrorCount, equalTo(1.0));
    }

    @Test
    public void testClientErrorEmitClientFailure() {
        AsyncResponder<String> responder = new AsyncResponder<>(asyncResponse, apiMetricsBundle);
        responder.error(new WebApplicationException("test", 400));

        ArgumentCaptor<WebApplicationException> argumentCaptor = ArgumentCaptor.forClass(WebApplicationException.class);
        verify(asyncResponse).resume(argumentCaptor.capture());

        final WebApplicationException exception = argumentCaptor.getValue();
        assertThat(exception.getResponse().getStatus(), equalTo(400));

        // Validate metrics
        Double invalidParameterCount = getMetricValueForCode("projects_create_requests", 400);
        assertThat(invalidParameterCount, equalTo(1.0));
    }

    private Double getMetricValueForCode(String metricName, int code) {
        return metricsRegistry.getSampleValue(metricName, new String[] { "result" }, new String[] { Integer.toString(code) });
    }
}
