package com.thetruemarket.api.infrastructure.job;

import com.thetruemarket.api.application.usecase.MarkStaleSkinAsSoldUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to clean up stale skins
 * Marks skins as SOLD when they haven't been seen by bots for configured duration
 *
 * Heartbeat Strategy:
 * - Every time a bot sees a skin, it sends a message → lastSeenAt is updated
 * - This job periodically checks for AVAILABLE skins without recent heartbeat
 * - Skins not seen for X hours (default: 2h) are marked as SOLD
 * - Sold skins are kept in database for historical analysis, not deleted
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "skin.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true // Enabled by default
)
public class CleanupStaleSkinsJob {
    private final MarkStaleSkinAsSoldUseCase markStaleSkinAsSoldUseCase;

    /**
     * Runs every 30 minutes (configurable) to mark stale skins as sold
     * Frequency: 1800000ms = 30 minutes
     */
    @Scheduled(fixedRateString = "${skin.cleanup.interval-ms:1800000}")
    public void cleanupStaleSkins() {
        log.info("Starting CleanupStaleSkinsJob");

        try {
            int markedCount = markStaleSkinAsSoldUseCase.execute();

            log.info("CleanupStaleSkinsJob completed: {} skins marked as SOLD", markedCount);

        } catch (Exception e) {
            log.error("Error in CleanupStaleSkinsJob: {}", e.getMessage(), e);
        }
    }
}
