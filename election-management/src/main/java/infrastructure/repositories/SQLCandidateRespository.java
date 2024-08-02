package infrastructure.repositories;

import domain.Candidate;
import domain.CandidateQuery;
import domain.CandidateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
// This annotation is need to make the class injectable by CDI(Java Contexts and Dependency Injection)
public class SQLCandidateRespository implements CandidateRepository {
  
  private final EntityManager entityManager; // This is the JPA EntityManager
  // that will be used to interact with the database
  
  public SQLCandidateRespository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }
  
  @Override
  @Transactional
  // This annotation is needed to make the method transactional and to allow
  // the EntityManager to manage the transaction
  public void save(List<Candidate> candidates) {
    candidates.stream().map(infrastructure.repositories.entities.Candidate::fromDomain).forEach(entityManager::merge);
  }
  
  @Override
  public List<Candidate> find(CandidateQuery query) {
    var criteriaQuery = getCandidateCriteriaQuery(query);
    return entityManager.createQuery(criteriaQuery)
            .getResultStream().map(infrastructure.repositories.entities.Candidate::toDomain).toList();
    
    //Stream is a sequence of elements supporting sequential and parallel
    // aggregate operations.
    // Stream operations are divided into intermediate and terminal operations.
    
  }
  
 
  
  @Override
  public List<Candidate> findAll() {
    var criteriaQuery = getCandidateCriteriaQuery(new CandidateQuery.Builder().build());
        return entityManager.createQuery(criteriaQuery).getResultStream()
            .map(infrastructure.repositories.entities.Candidate::toDomain)
            .toList();
  }
  
  private Predicate[] conditions(CandidateQuery query,
                                 CriteriaBuilder cb,
                                 Root<infrastructure.repositories.entities.Candidate> root) {
    return Stream.of(
                    query.ids().map(id -> cb.in(root.get("id")).value(id)),
                    query.name().map(name -> cb.or(
                            cb.like(cb.lower(root.get("familyName")), name.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("givenName")), name.toLowerCase() + "%")
                                                  ))
                    )
            .flatMap(Optional::stream)
            .toArray(Predicate[]::new);
  }
  
  private CriteriaQuery<infrastructure.repositories.entities.Candidate> getCandidateCriteriaQuery(CandidateQuery query) {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var criteriaQuery = criteriaBuilder.createQuery(infrastructure.repositories.entities.Candidate.class);
    var root = criteriaQuery.from(infrastructure.repositories.entities.Candidate.class);
    criteriaQuery.select(root).where(conditions(query, criteriaBuilder, root));
    return criteriaQuery;
  }
  
}
