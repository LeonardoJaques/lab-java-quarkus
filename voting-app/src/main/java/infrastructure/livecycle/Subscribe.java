package infrastructure.livecycle;

import domain.Election;
import infrastructure.repositories.RedisElectionRepository;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;


@Startup
// is a Quarkus annotation that marks the class as a bean that should be instantiated when the application starts
@ApplicationScoped
// is a CDI annotation that marks the class as a bean that lives as long as the application is running
public class Subscribe {
		private static final Logger LOGGER = Logger.getLogger(Subscribe.class);
		
		public Subscribe(ReactiveRedisDataSource reactiveRedisDataSource, RedisElectionRepository repository) {
				LOGGER.info("Starting the subscription");
				// Synchronous version
				Multi<String> sub = reactiveRedisDataSource.pubsub(String.class).subscribe("elections");
				sub.emitOn(Infrastructure.getDefaultWorkerPool()).subscribe().with(id -> {
						LOGGER.info("Election with id " + id + " has been published");
						Election election = repository.findById(id);
						LOGGER.info("Election with id " + id + " starting");
				});

// Asynchronous version
//				redisDataSource.pubsub(String.class).subscribe("elections", id -> {
//						LOGGER.info("Election with id " + id + " has been published");
//						Election election = repository.findById(id);
//						LOGGER.info("Election with id " + id + "starting");
//				});
		}
}
