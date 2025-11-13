package domain;

/**
 * Record representing the result of a single vote processing
 */
public record VoteResult(
        String voteId,
        boolean success,
        String errorMessage,
        long processingTimeMs) {
    public static VoteResult success(String voteId, long processingTimeMs) {
        return new VoteResult(voteId, true, null, processingTimeMs);
    }

    public static VoteResult failure(String voteId, String errorMessage, long processingTimeMs) {
        return new VoteResult(voteId, false, errorMessage, processingTimeMs);
    }
}
