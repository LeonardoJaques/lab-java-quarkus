package infrastructure.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * Interceptor for rate limiting using Bucket4j
 * Applies rate limits based on @RateLimited annotation
 */
@RateLimited
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class RateLimitInterceptor {

    private static final Logger LOG = Logger.getLogger(RateLimitInterceptor.class);

    @Inject
    RateLimiterConfig rateLimiterConfig;

    @AroundInvoke
    public Object rateLimit(InvocationContext context) throws Exception {
        // Get the rate limit annotation
        RateLimited rateLimited = context.getMethod().getAnnotation(RateLimited.class);
        if (rateLimited == null) {
            rateLimited = context.getTarget().getClass().getAnnotation(RateLimited.class);
        }

        if (rateLimited == null) {
            // No rate limit configured, proceed normally
            return context.proceed();
        }

        // Get user/IP identifier (simplified - in production, extract from request
        // context)
        String identifier = getUserIdentifier(context);

        // Get appropriate bucket
        Bucket bucket = getBucket(rateLimited.value(), identifier);

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Token consumed, request allowed
            long remainingTokens = probe.getRemainingTokens();
            LOG.debugf("Rate limit check passed for %s. Remaining tokens: %d", identifier, remainingTokens);
            return context.proceed();
        } else {
            // Rate limit exceeded
            long waitTime = probe.getNanosToWaitForRefill() / 1_000_000_000; // Convert to seconds
            LOG.warnf("Rate limit exceeded for %s. Wait %d seconds before retry", identifier, waitTime);

            // Throw rate limit exception (will be caught by exception mapper)
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded. Please try again in %d seconds", waitTime),
                    waitTime);
        }
    }

    /**
     * Get the appropriate bucket based on rate limit type
     */
    private Bucket getBucket(RateLimited.RateLimitType type, String identifier) {
        return switch (type) {
            case VOTING -> rateLimiterConfig.getVotingBucket(identifier);
            case QUERY -> rateLimiterConfig.getQueryBucket(identifier);
            case ADMIN -> rateLimiterConfig.getAdminBucket(identifier);
        };
    }

    /**
     * Get user identifier from context
     * In production, this would extract from JWT token or session
     * For now, using a simplified approach
     */
    private String getUserIdentifier(InvocationContext context) {
        // Try to get from method parameters
        Object[] params = context.getParameters();
        if (params != null && params.length > 0) {
            // Look for a parameter that might be a user ID or voter ID
            for (Object param : params) {
                if (param instanceof String str && !str.isEmpty()) {
                    return str;
                }
            }
        }

        // Fallback to method name + thread name (for testing)
        return context.getMethod().getName() + "-" + Thread.currentThread().getName();
    }

    /**
     * Exception thrown when rate limit is exceeded
     */
    public static class RateLimitExceededException extends RuntimeException {
        private final long waitTimeSeconds;

        public RateLimitExceededException(String message, long waitTimeSeconds) {
            super(message);
            this.waitTimeSeconds = waitTimeSeconds;
        }

        public long getWaitTimeSeconds() {
            return waitTimeSeconds;
        }
    }
}
