package infrastructure.repositories;

import domain.CandidateRepository;
import domain.CandidateRepositoryTest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
class SQLCandidateRespositoryTest extends CandidateRepositoryTest {

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