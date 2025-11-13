package infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Metrics for voting operations using Micrometer
 * Provides custom business metrics for monitoring and observability
 * 
 * Metrics exposed at /q/metrics (Prometheus format)
 */
@ApplicationScoped
public class VotingMetrics {

    // Counters
    private final Counter votesTotal;
    private final Counter votesSuccess;
    private final Counter votesFailed;
    private final Counter rateLimitHits;
    private final Counter circuitBreakerTrips;

    // Timers
    private final Timer voteProcessingTime;
    private final Timer batchProcessingTime;

    @Inject
    public VotingMetrics(MeterRegistry registry) {
        // Counters
        this.votesTotal = Counter.builder("votes.total")
                .description("Total number of vote attempts")
                .tag("application", "voting-app")
                .register(registry);

        this.votesSuccess = Counter.builder("votes.success")
                .description("Number of successful votes")
                .tag("application", "voting-app")
                .register(registry);

        this.votesFailed = Counter.builder("votes.failed")
                .description("Number of failed votes")
                .tag("application", "voting-app")
                .register(registry);

        this.rateLimitHits = Counter.builder("ratelimit.hits")
                .description("Number of rate limit violations")
                .tag("application", "voting-app")
                .register(registry);

        this.circuitBreakerTrips = Counter.builder("circuitbreaker.trips")
                .description("Number of circuit breaker activations")
                .tag("application", "voting-app")
                .register(registry);

        // Timers
        this.voteProcessingTime = Timer.builder("vote.processing.time")
                .description("Time taken to process a single vote")
                .tag("application", "voting-app")
                .register(registry);

        this.batchProcessingTime = Timer.builder("vote.batch.processing.time")
                .description("Time taken to process a batch of votes")
                .tag("application", "voting-app")
                .register(registry);
    }

    // Counter methods
    public void incrementVotesTotal() {
        votesTotal.increment();
    }

    public void incrementVotesSuccess() {
        votesSuccess.increment();
    }

    public void incrementVotesFailed() {
        votesFailed.increment();
    }

    public void incrementRateLimitHits() {
        rateLimitHits.increment();
    }

    public void incrementCircuitBreakerTrips() {
        circuitBreakerTrips.increment();
    }

    // Timer methods
    public Timer.Sample startVoteProcessing() {
        return Timer.start();
    }

    public void recordVoteProcessing(Timer.Sample sample) {
        sample.stop(voteProcessingTime);
    }

    public Timer.Sample startBatchProcessing() {
        return Timer.start();
    }

    public void recordBatchProcessing(Timer.Sample sample) {
        sample.stop(batchProcessingTime);
    }

    // Convenience method for timing code blocks
    public <T> T timeVoteProcessing(java.util.function.Supplier<T> supplier) {
        return voteProcessingTime.record(supplier);
    }

    public void timeVoteProcessing(Runnable runnable) {
        voteProcessingTime.record(runnable);
    }
}
