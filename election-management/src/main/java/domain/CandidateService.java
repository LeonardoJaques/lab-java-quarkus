package domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class CandidateService {
  private final CandidateRepository repository;

  @Inject
  public CandidateService(CandidateRepository repository) {
    this.repository = repository;
  }

  public void save(Candidate candidate) {
    repository.save(candidate);
  }

  public List<Candidate> findAll() {
    return repository.findAll();
  }

  public Candidate findById(String id) {
    return repository.findById(id).orElseThrow(NoSuchElementException::new);
  }
}