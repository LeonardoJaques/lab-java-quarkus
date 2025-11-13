package infrastructure.repositories;

import domain.Candidate;
import domain.Election;
import domain.ElectionRepository;
import domain.annotations.SQL;
import infrastructure.repositories.entities.ElectionCandidate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@SQL
@ApplicationScoped
public class SQLElectionRepository implements ElectionRepository {
    private static final Logger LOG = Logger.getLogger(SQLElectionRepository.class);

    private final EntityManager entityManager;

    public SQLElectionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void submit(Election election) {
        try {
            entityManager.merge(infrastructure.repositories.entities.Election.fromDomain(election));
            election.votes()
                    .entrySet()
                    .stream()
                    .map(entry -> infrastructure.repositories.entities.ElectionCandidate.fromDomain(election,
                            entry.getKey(), entry.getValue()))
                    .forEach(entityManager::merge);
            LOG.infof("Election %s submitted successfully to database", election.id());
        } catch (Exception e) {
            LOG.errorf(e, "Error submitting election %s to database", election.id());
            throw new RuntimeException("Failed to submit election to database", e);
        }
    }

    @Override
    public List<Election> findAll() {
        try {
            Stream<Object[]> stream = entityManager.createNativeQuery(
                    "SELECT e.id AS election_id, c.id AS candidate_id, c.photo, c.given_name, c.family_name, c.email, c.phone, c.job_title, ec.votes FROM elections AS e INNER JOIN election_candidate AS ec ON ec.election_id = e.id INNER JOIN candidates AS c ON ec.candidate_id = c.id")
                    .getResultStream();

            Map<String, List<Object[]>> map = stream.collect(groupingBy(o -> o[0].toString()));

            return map.entrySet()
                    .stream()
                    .map(entry -> {
                        Map.Entry<Candidate, Integer>[] candidates = entry.getValue()
                                .stream()
                                .map(row -> Map.entry(new Candidate(row[1].toString(),
                                        Optional.ofNullable(row[2].toString()),
                                        row[3].toString(),
                                        row[4].toString(),
                                        row[5].toString(),
                                        Optional.ofNullable(row[6].toString()),
                                        Optional.ofNullable(row[7].toString())),
                                        (Integer) row[8]))
                                .toArray(Map.Entry[]::new);

                        return new Election(entry.getKey(), Map.ofEntries(candidates));
                    }).toList();
        } catch (Exception e) {
            LOG.errorf(e, "Error finding all elections from database");
            throw new RuntimeException("Failed to find elections from database", e);
        }
    }

    @Override
    @Transactional
    public Election sync(Election election) {
        try {
            election.votes()
                    .entrySet()
                    .stream()
                    .map(entry -> ElectionCandidate.fromDomain(election, entry.getKey(), entry.getValue()))
                    .forEach(entityManager::merge);
            LOG.infof("Election %s synced successfully to database", election.id());
            return election;
        } catch (Exception e) {
            LOG.errorf(e, "Error syncing election %s to database", election.id());
            throw new RuntimeException("Failed to sync election to database", e);
        }
    }
}