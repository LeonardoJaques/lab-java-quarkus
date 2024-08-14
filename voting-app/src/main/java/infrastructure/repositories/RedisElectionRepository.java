package infrastructure.repositories;

import domain.Candidate;
import domain.Election;
import domain.ElectionRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RedisElectionRepository implements ElectionRepository {
		private static final Logger LOGGER = Logger.getLogger(RedisElectionRepository.class);
		private final SortedSetCommands<String, String> sortedSetCommands;
		
		public RedisElectionRepository(RedisDataSource redisDataSources) {
				sortedSetCommands = redisDataSources.sortedSet(String.class, String.class);
		}
		
		@Override
		public Election findById(String id) {
				LOGGER.info("Finding election with id " + id);
				List<Candidate> candidates = sortedSetCommands
								.zrange("election:" + id, 0, -1)
								.stream()
								.map(Candidate::new)
								.toList();
				
				return new Election(id, candidates);
		}
}
		

