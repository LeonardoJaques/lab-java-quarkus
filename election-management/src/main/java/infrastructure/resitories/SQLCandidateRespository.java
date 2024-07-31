package infrastructure.resitories;

import domain.Candidate;
import domain.CandidateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped // This annotation is need to make the class injectable by CDI(Java Contexts and Dependency Injection)
public class SQLCandidateRespository implements CandidateRepository {

  @Override
  public void save(List<Candidate> candidates) {
  }

  @Override
  public List<Candidate> findAll() {
    return List.of();
  }
}
