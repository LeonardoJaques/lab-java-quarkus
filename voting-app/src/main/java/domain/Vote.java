package domain;

/**
 * Record representing a single vote
 * Immutable and lightweight for processing with Virtual Threads
 */
public record Vote(
        String electionId,
        String candidateId,
        String voterId,
        long timestamp) {
    public Vote {
        if (electionId == null || electionId.isBlank()) {
            throw new IllegalArgumentException("Election ID cannot be null or blank");
        }
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("Candidate ID cannot be null or blank");
        }
        if (voterId == null || voterId.isBlank()) {
            throw new IllegalArgumentException("Voter ID cannot be null or blank");
        }
    }

    public static Vote create(String electionId, String candidateId, String voterId) {
        return new Vote(electionId, candidateId, voterId, System.currentTimeMillis());
    }
}
