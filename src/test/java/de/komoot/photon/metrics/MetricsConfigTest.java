package de.komoot.photon.metrics;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class MetricsConfigTest {
    OpenSearchClient openSearchClient = new OpenSearchClient(null) {};

    @Test
    void testInit() {
        assertThat(MetricsConfig.setupMetrics("prometheus", openSearchClient))
                .satisfies(
                        c -> assertThat(c.getRegistry()).isNotNull(),
                        c -> assertThat(c.getPlugin()).isNotNull(),
                        c -> assertThat(c.isEnabled()).isTrue()
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testNoInit(String metricsType) {
        assertThat(MetricsConfig.setupMetrics(metricsType, openSearchClient))
                .satisfies(
                        c -> assertThatExceptionOfType(IllegalStateException.class).isThrownBy(c::getRegistry),
                        c -> assertThatExceptionOfType(IllegalStateException.class).isThrownBy(c::getPlugin),
                        c -> assertThat(c.isEnabled()).isFalse()
                );
    }

    /**
     * The HTTP request-latency timer that Javalin's MicrometerPlugin registers is named
     * "http.server.requests". The meter filter must enable percentile histograms for it so that
     * Prometheus gets "_bucket" series with an "le" label, which histogram_quantile() (p95/p99)
     * depends on. This guards the name the filter matches against accidental edits; the end-to-end
     * guard against Javalin renaming the emitted meter lives in ApiMetricsTest.
     */
    @Test
    void testHttpServerRequestsTimerExposesHistogramBuckets() {
        MetricsConfig metricsConfig = MetricsConfig.setupMetrics("prometheus", openSearchClient);
        PrometheusMeterRegistry registry = metricsConfig.getRegistry();

        // Mirror the timer emitted by io.javalin.micrometer.MicrometerPlugin for each request.
        registry.timer("http.server.requests", "method", "GET", "uri", "/api", "status", "200")
                .record(Duration.ofMillis(5));

        assertThat(registry.scrape())
                .contains("http_server_requests_seconds_bucket")
                .contains("le=\"")
                .contains("http_server_requests_seconds_count")
                .contains("http_server_requests_seconds_sum");
    }

    /**
     * The histogram must stay scoped to the request timer. Enabling percentile histograms globally
     * would add high-cardinality buckets to unrelated timers (JVM GC pauses, OpenSearch client
     * timers, etc.), so an arbitrary timer must not get "_bucket" series.
     */
    @Test
    void testUnrelatedTimerDoesNotExposeHistogramBuckets() {
        MetricsConfig metricsConfig = MetricsConfig.setupMetrics("prometheus", openSearchClient);
        PrometheusMeterRegistry registry = metricsConfig.getRegistry();

        registry.timer("some.unrelated.timer").record(Duration.ofMillis(5));

        assertThat(registry.scrape())
                .doesNotContain("some_unrelated_timer_seconds_bucket");
    }
}
