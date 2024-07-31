package domain;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CandidateServiceTest {


  @Inject
  CandidateService service;

  @InjectMock
  CandidateRepository repository;


  @Test()
  void save() {
    Candidate candidate = Instancio.create(Candidate.class);
    service.save(candidate);
    verify(repository).save(candidate);
    verifyNoMoreInteractions(repository);
  }
  @Test()
  void findAll() {
    service.findAll();

  }
}