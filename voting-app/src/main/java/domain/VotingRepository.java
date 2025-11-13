package domain;

/**
 * Repository interface for Vote operations
 */
public interface VotingRepository {

    /**
     * Save a vote to the repository
     * 
     * @param vote the vote to save
     */
    void save(Vote vote);

    /**
     * Count votes for a specific candidate
     * 
     * @param candidateId the candidate ID
     * @return the vote count
     */
    long countVotesByCandidate(String candidateId);

    /**
     * Count votes for a specific election
     * 
     * @param electionId the election ID
     * @return the vote count
     */
    long countVotesByElection(String electionId);
}
