package infrastructure.livecycle;

import infrastructure.repositories.RedisElectionRepository;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;


@Startup
// is a Quarkus annotation that marks the class as a bean that should be instantiated when the application starts
@ApplicationScoped
// is a CDI annotation that marks the class as a bean that lives as long as the application is running
public class Subscribe {
    private static final Logger LOGGER = Logger.getLogger(Subscribe.class);

    public Subscribe(ReactiveRedisDataSource dataSource,
                     RedisElectionRepository repository) {
        LOGGER.info("Startup: Subscribe");

        dataSource.pubsub(String.class)
                .subscribe("elections")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with(id -> {
                    LOGGER.info("Election " + id + " received from subscription");
                    LOGGER.info("Election " + repository.findById(id) + " starting");
                });
    }
}
