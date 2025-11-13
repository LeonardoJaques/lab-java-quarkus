package infrastructure.repositories;

import domain.Candidate;
import domain.Election;
import domain.ElectionRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.*;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class RedisElectionRepository implements ElectionRepository {
    private static final Logger LOG = Logger.getLogger(RedisElectionRepository.class);

    // Fallback cache for circuit breaker
    private final Map<String, Election> fallbackCache = new ConcurrentHashMap<>();

    private final PubSubCommands<String> pubsub;
    private final SortedSetCommands<String, String> commands;

    public RedisElectionRepository(RedisDataSource dataSource) {
        commands = dataSource.sortedSet(String.class, String.class);
        pubsub = dataSource.pubsub(String.class);
    }

    @Override
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.5, delay = 10000, delayUnit = ChronoUnit.MILLIS)
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "submitFallback")
    public void submit(Election election) {
        try {
            Map<String, Double> rank = election.votes()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().id(),
                            entry -> entry.getValue().doubleValue()));
            commands.zadd("election:" + election.id(), rank);
            pubsub.publish("elections", election.id());

            // Cache successful submission
            fallbackCache.put(election.id(), election);

            LOG.infof("Election %s submitted successfully to Redis", election.id());
        } catch (Exception e) {
            LOG.errorf(e, "Error submitting election %s to Redis", election.id());
            throw new RuntimeException("Failed to submit election to Redis", e);
        }
    }

    /**
     * Fallback method for submit when Redis is unavailable
     */
    private void submitFallback(Election election) {
        LOG.warnf("Using fallback for election %s submission - Redis unavailable", election.id());
        fallbackCache.put(election.id(), election);
    }

    @Override
    public List<Election> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.5, delay = 10000, delayUnit = ChronoUnit.MILLIS)
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "syncFallback")
    public Election sync(Election election) {
        try {
            var map = commands.zrangebyscoreWithScores("election:" + election.id(),
                    ScoreRange.from(Integer.MIN_VALUE, Integer.MAX_VALUE))
                    .stream()
                    .map(scoredValue -> {
                        Candidate candidate = election.votes()
                                .keySet()
                                .stream()
                                .filter(c -> c.id().equals(scoredValue.value()))
                                .findFirst()
                                .orElseThrow();

                        return Map.entry(candidate, (int) scoredValue.score());
                    })
                    .toArray(Map.Entry[]::new);

            @SuppressWarnings("unchecked")
            Election syncedElection = new Election(election.id(), Map.ofEntries(map));

            // Cache successful sync
            fallbackCache.put(election.id(), syncedElection);

            LOG.infof("Election %s synced successfully from Redis", election.id());
            return syncedElection;
        } catch (Exception e) {
            LOG.errorf(e, "Error syncing election %s from Redis", election.id());
            throw new RuntimeException("Failed to sync election from Redis", e);
        }
    }

    /**
     * Fallback method for sync when Redis is unavailable
     */
    private Election syncFallback(Election election) {
        LOG.warnf("Using fallback for election %s sync - Redis unavailable", election.id());
        Election cached = fallbackCache.get(election.id());
        if (cached != null) {
            LOG.infof("Returning cached election %s", election.id());
            return cached;
        }
        LOG.warnf("No cached data for election %s, returning original", election.id());
        return election;
    }
}