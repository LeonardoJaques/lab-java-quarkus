package api;

import domain.CandidateService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
class CandidateApiTest {
		@Inject
		CandidateApi candidateApi;
		
		@InjectMock
		CandidateService candidateService;
		
		@Test
		void create() {
				var dto = Instancio.create(api.dto.in.CreateCandidate.class);
				ArgumentCaptor<domain.Candidate> captor = ArgumentCaptor.forClass(domain.Candidate.class);
				candidateApi.create(dto);
				verify(candidateService).save(captor.capture());
				verifyNoMoreInteractions(candidateService);
				
				var candidate = captor.getValue();
				assertEquals(dto.photo(), candidate.photo());
				assertEquals(dto.givenName(), candidate.givenName());
				assertEquals(dto.familyName(), candidate.familyName());
				assertEquals(dto.email(), candidate.email());
				assertEquals(dto.phone(), candidate.phone());
				assertEquals(dto.JobTitle(), candidate.jobTitle());
				
		}
		
		@Test
		void update() {
				String id = UUID.randomUUID().toString();
				var dto = Instancio.create(api.dto.in.UpdateCandidate.class);
				var candidate = dto.toDomain(id);
				
				ArgumentCaptor<domain.Candidate> captor = ArgumentCaptor.forClass(domain.Candidate.class);
				when(candidateService.findById(id)).thenReturn(candidate);
				var response = candidateApi.update(id, dto);
				
				verify(candidateService).save(captor.capture());
				verify(candidateService).findById(id);
				verifyNoMoreInteractions(candidateService);
				
				assertEquals(api.dto.out.Candidate.fromDomain(candidate), response);
		}
		
		@Test
		void list(){
				var candidate = Instancio.stream(domain.Candidate.class).limit(10).toList();
				when(candidateService.findAll()).thenReturn(candidate);
				
				var response = candidateApi.list();
				
				verify(candidateService).findAll();
				verifyNoMoreInteractions(candidateService);
				
				assertEquals(candidate.stream().map(api.dto.out.Candidate::fromDomain).toList(), response);
				
		}
		
		
}