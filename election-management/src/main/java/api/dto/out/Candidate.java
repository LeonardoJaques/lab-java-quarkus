package api.dto.out;

import java.util.Optional;

public record Candidate(Optional<String> photo, String givenName,
												String fullName, String email,
												Optional<String> phone,
												Optional<String> JobTitle) {
		
		public static Candidate fromDomain(domain.Candidate candidate) {
				return new Candidate(candidate.photo(), candidate.givenName(), candidate.givenName() + " " + candidate.familyName(), candidate.email(), candidate.phone(), candidate.jobTitle());
		}
		
}
