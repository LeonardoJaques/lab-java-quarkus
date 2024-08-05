package api;

import api.dto.out.Candidate;
import domain.CandidateService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

//Make a conection between the CandidateService and the CandidateController
//Facade pattern
@ApplicationScoped
public class CandidateApi {
		private final CandidateService candidateService;
		
		public CandidateApi(CandidateService candidateService) {this.candidateService = candidateService;}
		
		public void create(api.dto.in.CreateCandidate createCandidateDto) {
				candidateService.save(createCandidateDto.toDomain());
		}
		
		public api.dto.out.Candidate update(String id, api.dto.in.UpdateCandidate dto) {
				candidateService.save(dto.toDomain(id));
				return api.dto.out.Candidate.fromDomain(candidateService.findById(id));
		}
		
		public List<api.dto.out.Candidate> list() {
				return candidateService.findAll().stream()
						.map(api.dto.out.Candidate::fromDomain)
						.toList();
		};
}
