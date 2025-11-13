package domain;

import java.util.List;

/**
 * Record representing the result of batch vote processing
 */
public record BatchVoteResult(
        int totalVotes,
        int successCount,
        int failureCount,
        long totalProcessingTimeMs,
        double averageTimePerVote,
        List<VoteResult> results) {
    public BatchVoteResult {
        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null");
        }
    }

    public static BatchVoteResult from(List<VoteResult> results, long totalProcessingTimeMs) {
        int total = results.size();
        int success = (int) results.stream().filter(VoteResult::success).count();
        int failure = total - success;
        double avgTime = total > 0 ? (double) totalProcessingTimeMs / total : 0;

        return new BatchVoteResult(total, success, failure, totalProcessingTimeMs, avgTime, results);
    }

    public double getSuccessRate() {
        return totalVotes > 0 ? (double) successCount / totalVotes * 100 : 0;
    }
}
