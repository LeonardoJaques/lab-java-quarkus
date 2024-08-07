package api.dto.in;

import com.fasterxml.jackson.annotation.JsonInclude;
import domain.Candidate;

import java.util.Optional;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record UpdateCandidate(Optional<String> photo, String givenName,
															String familyName, String email,
															Optional<String> phone,
															Optional<String> jobTitle) {
		
		public Candidate toDomain(String id) {
				return new Candidate(id, photo, givenName, familyName, email, phone,
														 jobTitle);
		}
}
