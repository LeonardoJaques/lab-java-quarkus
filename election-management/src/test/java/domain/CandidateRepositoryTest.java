package domain;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//testar a intarface desacopla a suite de testes da implementação
// possibilitando a troca de implementação sem alterar a suite de testes

public abstract class CandidateRepositoryTest {
		
		public abstract CandidateRepository repository();
		
		@Test
		void save() {
				Candidate candidate = Instancio.create(Candidate.class);
				repository().save(candidate);
				var result = repository().findById(candidate.id());
				assertTrue(result.isPresent());
				assertEquals(candidate, result.get());
		}
		
		@Test
		void findAll() {
				List<Candidate> candidates = Instancio.stream(Candidate.class).limit(10).toList();
				repository().save(candidates);
				List<Candidate> result = repository().findAll();
				assertEquals(result.size(), candidates.size());
				
		}
		
		@Test
		void findByName() {
				Candidate candidate1 = Instancio.create(Candidate.class);
				Candidate candidate2 = Instancio.of(Candidate.class).set(field("familyName"), "Jaques").create();
				CandidateQuery query = new CandidateQuery.Builder().name("JAQ").build();
				repository().save(List.of(candidate1, candidate2));
				List<Candidate> result = repository().find(query);
				assertEquals(result.size(), 1);
				assertEquals(result.get(0), candidate2);
		}
  
  
		
}