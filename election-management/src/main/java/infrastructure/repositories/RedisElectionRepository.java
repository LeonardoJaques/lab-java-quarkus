package infrastructure.repositories;

import domain.Candidate;
import domain.Election;
import domain.ElectionRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RedisElectionRepository implements ElectionRepository {
    private static final Logger LOG = Logger.getLogger(RedisElectionRepository.class);

    private final PubSubCommands<String> pubsub;
    private final SortedSetCommands<String, String> commands;

    public RedisElectionRepository(RedisDataSource dataSource) {
        commands = dataSource.sortedSet(String.class, String.class);
        pubsub = dataSource.pubsub(String.class);
    }

    @Override
    public void submit(Election election) {
        try {
            Map<String, Double> rank = election.votes()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().id(),
                            entry -> entry.getValue().doubleValue()));
            commands.zadd("election:" + election.id(), rank);
            pubsub.publish("elections", election.id());
            LOG.infof("Election %s submitted successfully to Redis", election.id());
        } catch (Exception e) {
            LOG.errorf(e, "Error submitting election %s to Redis", election.id());
            throw new RuntimeException("Failed to submit election to Redis", e);
        }
    }

    @Override
    public List<Election> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
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

            Election syncedElection = new Election(election.id(), Map.ofEntries(map));
            LOG.infof("Election %s synced successfully from Redis", election.id());
            return syncedElection;
        } catch (Exception e) {
            LOG.errorf(e, "Error syncing election %s from Redis", election.id());
            throw new RuntimeException("Failed to sync election from Redis", e);
        }
    }
}