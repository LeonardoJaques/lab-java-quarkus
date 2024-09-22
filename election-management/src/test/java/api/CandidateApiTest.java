package api;

import domain.CandidateRepository;
import domain.CandidateRepositoryTest;
import infrastructure.repositories.SQLElectionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;

import javax.inject.Inject;

@QuarkusTest
class SQLCandidateRepositoryTest extends CandidateRepositoryTest {
	@Inject
	CandidateRepository repository;

	@Inject
	EntityManager entityManager;

	@Override
	public CandidateRepository repository() {
		return repository;
	}

	@AfterEach
	@TestTransaction
	void tearDown() {
		entityManager.createNativeQuery("TRUNCATE TABLE candidates").executeUpdate();
	}
}