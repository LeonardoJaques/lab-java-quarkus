package infrastructure.repositories;

import domain.Vote;
import domain.VotingRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.*;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;

/**
 * Redis-based implementation of VotingRepository
 * Stores votes in Redis for high-performance access
 */
@ApplicationScoped
public class RedisVotingRepository implements VotingRepository {

    private static final Logger LOG = Logger.getLogger(RedisVotingRepository.class);
    private static final String VOTE_KEY_PREFIX = "vote:";
    private static final String CANDIDATE_VOTES_PREFIX = "candidate:votes:";
    private static final String ELECTION_VOTES_PREFIX = "election:votes:";

    @Inject
    RedisDataSource redisDataSource;

    private ValueCommands<String, String> valueCommands;

    @jakarta.annotation.PostConstruct
    void init() {
        this.valueCommands = redisDataSource.value(String.class);
    }

    @Override
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 10000, delayUnit = ChronoUnit.MILLIS)
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "saveFallback")
    public void save(Vote vote) {
        try {
            // Save vote
            String voteKey = VOTE_KEY_PREFIX + vote.voterId() + ":" + vote.electionId();
            valueCommands.set(voteKey, vote.candidateId());

            // Increment candidate vote count
            String candidateKey = CANDIDATE_VOTES_PREFIX + vote.candidateId();
            redisDataSource.value(Long.class).incr(candidateKey);

            // Increment election vote count
            String electionKey = ELECTION_VOTES_PREFIX + vote.electionId();
            redisDataSource.value(Long.class).incr(electionKey);

            LOG.debugf("Vote saved: voter=%s, candidate=%s, election=%s",
                    vote.voterId(), vote.candidateId(), vote.electionId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save vote for voter %s", vote.voterId());
            throw new RuntimeException("Failed to save vote", e);
        }
    }

    /**
     * Fallback method when Redis is unavailable
     * Logs the failed vote for later processing (could queue to database)
     */
    @SuppressWarnings("unused")
    private void saveFallback(Vote vote) {
        LOG.warnf("Using fallback for vote save - Redis unavailable. Vote: voter=%s, candidate=%s",
                vote.voterId(), vote.candidateId());
        // In production, this could queue the vote to a database or message queue
    }

    @Override
    public long countVotesByCandidate(String candidateId) {
        try {
            String key = CANDIDATE_VOTES_PREFIX + candidateId;
            Long count = redisDataSource.value(Long.class).get(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to count votes for candidate %s", candidateId);
            return 0L;
        }
    }

    @Override
    public long countVotesByElection(String electionId) {
        try {
            String key = ELECTION_VOTES_PREFIX + electionId;
            Long count = redisDataSource.value(Long.class).get(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to count votes for election %s", electionId);
            return 0L;
        }
    }
}
