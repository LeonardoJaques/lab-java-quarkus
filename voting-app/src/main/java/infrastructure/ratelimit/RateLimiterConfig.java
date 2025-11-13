package infrastructure.ratelimit;

import jakarta.enterprise.context.ApplicationScoped;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter Configuration using Bucket4j
 * Implements token bucket algorithm for rate limiting
 * 
 * Bucket Types:
 * 1. Voting Rate Limit: 10 votes per minute per user (anti-fraud)
 * 2. Query Rate Limit: 100 requests per minute per IP
 * 3. Admin Rate Limit: 50 requests per minute per user
 */
@ApplicationScoped
public class RateLimiterConfig {

    private static final Logger LOG = Logger.getLogger(RateLimiterConfig.class);

    // Store buckets per user/IP
    private final Map<String, Bucket> votingBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> queryBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> adminBuckets = new ConcurrentHashMap<>();

    /**
     * Get or create voting bucket for a user
     * Rate: 10 votes per minute (anti-fraud protection)
     */
    public Bucket getVotingBucket(String userId) {
        return votingBuckets.computeIfAbsent(userId, k -> createVotingBucket());
    }

    /**
     * Get or create query bucket for an IP
     * Rate: 100 requests per minute (DDoS protection)
     */
    public Bucket getQueryBucket(String ipAddress) {
        return queryBuckets.computeIfAbsent(ipAddress, k -> createQueryBucket());
    }

    /**
     * Get or create admin bucket for a user
     * Rate: 50 requests per minute (admin operations)
     */
    public Bucket getAdminBucket(String userId) {
        return adminBuckets.computeIfAbsent(userId, k -> createAdminBucket());
    }

    /**
     * Create voting bucket: 10 votes per minute
     * Refills at rate of 1 token every 6 seconds
     */
    private Bucket createVotingBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        LOG.debug("Created voting rate limit bucket: 10 votes/minute");
        return bucket;
    }

    /**
     * Create query bucket: 100 requests per minute
     * Refills at rate of 100 tokens every 60 seconds
     */
    private Bucket createQueryBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        LOG.debug("Created query rate limit bucket: 100 req/minute");
        return bucket;
    }

    /**
     * Create admin bucket: 50 requests per minute
     * Refills at rate of 50 tokens every 60 seconds
     */
    private Bucket createAdminBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(50)
                .refillIntervally(50, Duration.ofMinutes(1))
                .build();
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        LOG.debug("Created admin rate limit bucket: 50 req/minute");
        return bucket;
    }

    /**
     * Clear all buckets (for testing or reset)
     */
    public void clearAll() {
        votingBuckets.clear();
        queryBuckets.clear();
        adminBuckets.clear();
        LOG.info("All rate limit buckets cleared");
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "votingBuckets", votingBuckets.size(),
                "queryBuckets", queryBuckets.size(),
                "adminBuckets", adminBuckets.size(),
                "totalBuckets", votingBuckets.size() + queryBuckets.size() + adminBuckets.size());
    }
}
