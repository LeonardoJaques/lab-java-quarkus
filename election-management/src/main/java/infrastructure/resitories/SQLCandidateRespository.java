package infrastructure.resitories;

import domain.Candidate;
import domain.CandidateQuery;
import domain.CandidateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped // This annotation is need to make the class injectable by CDI(Java Contexts and Dependency Injection)
public class SQLCandidateRespository implements CandidateRepository {

  @Override
  public List<Candidate> find() {
    return CandidateRepository.super.find();
  }

  @Override
  public void save(List<Candidate> candidates) {
  }

  @Override
  public List<Candidate> find(CandidateQuery query) {
    return List.of();
  }

  @Override
  public List<Candidate> findAll() {
    return List.of();
  }


}
