package domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * VotingService with Virtual Threads support
 * Provides 3 processing strategies:
 * 1. Sequential - Traditional processing (baseline)
 * 2. Platform Threads - Fixed thread pool (traditional concurrent)
 * 3. Virtual Threads - Java 21+ lightweight threads (high performance)
 */
@ApplicationScoped
public class VotingService {

    private static final Logger LOG = Logger.getLogger(VotingService.class);

    @Inject
    VotingRepository repository;

    // Configuration
    private static final int PLATFORM_THREAD_POOL_SIZE = 10;

    /**
     * Processing Strategy Enum
     */
    public enum ProcessingStrategy {
        SEQUENTIAL,
        PLATFORM_THREADS,
        VIRTUAL_THREADS
    }

    /**
     * Process votes sequentially (baseline - slowest)
     */
    public BatchVoteResult processSequential(List<Vote> votes) {
        LOG.infof("Processing %d votes SEQUENTIALLY", votes.size());
        long startTime = System.currentTimeMillis();

        List<VoteResult> results = new ArrayList<>();

        for (Vote vote : votes) {
            long voteStart = System.currentTimeMillis();
            try {
                processVote(vote);
                long processingTime = System.currentTimeMillis() - voteStart;
                results.add(VoteResult.success(vote.candidateId(), processingTime));
            } catch (Exception e) {
                long processingTime = System.currentTimeMillis() - voteStart;
                results.add(VoteResult.failure(vote.candidateId(), e.getMessage(), processingTime));
                LOG.errorf(e, "Failed to process vote for candidate %s", vote.candidateId());
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        LOG.infof("Sequential processing completed in %dms", totalTime);

        return BatchVoteResult.from(results, totalTime);
    }

    /**
     * Process votes with platform threads (traditional concurrent - medium)
     */
    public BatchVoteResult processWithPlatformThreads(List<Vote> votes) throws InterruptedException {
        LOG.infof("Processing %d votes with PLATFORM THREADS (pool size: %d)",
                votes.size(), PLATFORM_THREAD_POOL_SIZE);
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(PLATFORM_THREAD_POOL_SIZE);
        List<Future<VoteResult>> futures = new ArrayList<>();

        try {
            for (Vote vote : votes) {
                Future<VoteResult> future = executor.submit(() -> {
                    long voteStart = System.currentTimeMillis();
                    try {
                        processVote(vote);
                        long processingTime = System.currentTimeMillis() - voteStart;
                        return VoteResult.success(vote.candidateId(), processingTime);
                    } catch (Exception e) {
                        long processingTime = System.currentTimeMillis() - voteStart;
                        LOG.errorf(e, "Failed to process vote for candidate %s", vote.candidateId());
                        return VoteResult.failure(vote.candidateId(), e.getMessage(), processingTime);
                    }
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            List<VoteResult> results = new ArrayList<>();
            for (Future<VoteResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    LOG.error("Task execution failed", e);
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            LOG.infof("Platform threads processing completed in %dms", totalTime);

            return BatchVoteResult.from(results, totalTime);

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Process votes with virtual threads (Java 21+ - fastest)
     * Virtual threads are lightweight and can handle massive concurrency
     */
    public BatchVoteResult processWithVirtualThreads(List<Vote> votes) throws InterruptedException {
        LOG.infof("Processing %d votes with VIRTUAL THREADS", votes.size());
        long startTime = System.currentTimeMillis();

        // Java 21+ Virtual Thread Executor
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        LOG.info("✓ Using Virtual Threads (Java 21+)");

        List<Future<VoteResult>> futures = new ArrayList<>();

        try {
            for (Vote vote : votes) {
                Future<VoteResult> future = executor.submit(() -> {
                    long voteStart = System.currentTimeMillis();
                    try {
                        processVote(vote);
                        long processingTime = System.currentTimeMillis() - voteStart;
                        return VoteResult.success(vote.candidateId(), processingTime);
                    } catch (Exception e) {
                        long processingTime = System.currentTimeMillis() - voteStart;
                        LOG.errorf(e, "Failed to process vote for candidate %s", vote.candidateId());
                        return VoteResult.failure(vote.candidateId(), e.getMessage(), processingTime);
                    }
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            List<VoteResult> results = new ArrayList<>();
            for (Future<VoteResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    LOG.error("Task execution failed", e);
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            LOG.infof("Virtual threads processing completed in %dms", totalTime);

            return BatchVoteResult.from(results, totalTime);

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Auto-select best strategy based on vote count
     */
    public BatchVoteResult processAuto(List<Vote> votes) throws InterruptedException {
        int voteCount = votes.size();

        if (voteCount <= 10) {
            // For small batches, sequential is fine
            return processSequential(votes);
        } else if (voteCount <= 100) {
            // For medium batches, platform threads are good
            return processWithPlatformThreads(votes);
        } else {
            // For large batches, virtual threads excel
            return processWithVirtualThreads(votes);
        }
    }

    /**
     * Process a single vote (simulates database/cache operations)
     */
    private void processVote(Vote vote) {
        // Simulate some processing time (database write, cache update, etc.)
        try {
            repository.save(vote);
            // Simulate additional processing
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Vote processing interrupted", e);
        }
    }
}
