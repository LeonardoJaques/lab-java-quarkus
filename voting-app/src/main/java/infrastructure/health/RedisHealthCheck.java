package infrastructure.health;

import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.jboss.logging.Logger;

/**
 * Redis Health Check
 * Checks if Redis is available and responding
 * 
 * @Liveness - Application cannot function without Redis
 */
@Liveness
@ApplicationScoped
public class RedisHealthCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(RedisHealthCheck.class);

    @Inject
    RedisDataSource redisDataSource;

    @Override
    public HealthCheckResponse call() {
        try {
            // Try a simple ping operation
            redisDataSource.value(String.class).get("health-check-ping");

            LOG.debug("Redis health check passed");
            return HealthCheckResponse.builder()
                    .name("Redis connection health check")
                    .status(true)
                    .withData("connection", "UP")
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Redis health check failed");
            return HealthCheckResponse.builder()
                    .name("Redis connection health check")
                    .status(false)
                    .withData("connection", "DOWN")
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
