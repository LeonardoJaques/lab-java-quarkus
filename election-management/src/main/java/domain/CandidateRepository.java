package domain;

import domain.CandidateQuery.Builder;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CandidateRepository {

  default Optional<Candidate> findById(String id) {
    CandidateQuery query = new CandidateQuery.Builder().ids(Set.of(id)).build();
    return find(query).stream().findFirst();

  }
  default void save(Candidate candidate) {
    save(List.of(candidate));
  }

  default List<Candidate> find() {
    return find(new CandidateQuery.Builder().build());
  }

  void save(List<Candidate> candidates);

  List<Candidate> find(CandidateQuery query);

  List<Candidate> findAll();


}
