package api;

import domain.CandidateRepository;
import domain.CandidateRepositoryTest;
import infrastructure.repositories.SQLElectionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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

	@BeforeEach
	@TestTransaction
	void setUp() {
		entityManager.createNativeQuery("TRUNCATE TABLE candidates").executeUpdate();
	}

	@AfterEach
	@TestTransaction
	void tearDown() {
		entityManager.createNativeQuery("TRUNCATE TABLE candidates").executeUpdate();
	}
}