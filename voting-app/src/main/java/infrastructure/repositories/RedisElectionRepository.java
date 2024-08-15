package infrastructure.repositories;

import domain.Candidate;
import domain.Election;
import domain.ElectionRepository;
import io.quarkus.cache.CacheResult;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RedisElectionRepository implements ElectionRepository {
		
		private static final Logger LOGGER = Logger.getLogger(RedisElectionRepository.class);
		private final SortedSetCommands<String, String> sortedSetCommands;
		private final KeyCommands<String> keyCommands;
		
		public RedisElectionRepository(RedisDataSource redisDataSources) {
				sortedSetCommands = redisDataSources.sortedSet(String.class, String.class);
				keyCommands = redisDataSources.key(String.class);
		}
		
		@Override
		@CacheResult(cacheName = "memoization")
		public Election findById(String id) {
				LOGGER.info("Finding election with id " + id);
				List<Candidate> candidates = sortedSetCommands
								.zrange("election:" + id, 0, -1)
								.stream()
								.map(Candidate::new)
								.toList();
				
				return new Election(id, candidates);
		}
		
		@Override
		public List<Election> findAll() {
				LOGGER.info("Retrieving all elections");
				return keyCommands.keys("election:*")
								.stream()
								.map(id -> findById(id.replace("election:", "")))
								.toList();
				
		}
		
		@Override
		public void vote(String electionId, String candidateId) {
				LOGGER.info("Voting for candidate " + candidateId + " in election " + electionId);
				sortedSetCommands.zincrby("election:" + electionId, 1, candidateId);
		
		}
}
		

