package domain;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CandidateService {

  //this injection is more recommended than the constructor injection because it is more readable and performant
  private final CandidateRepository repository;

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
    return repository.findById(id).orElseThrow();
  }
}
