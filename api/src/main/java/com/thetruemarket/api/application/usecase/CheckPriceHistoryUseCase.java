package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.repository.SteamPriceHistoryRepository;
import com.thetruemarket.api.domain.valueobject.Wear;
import com.thetruemarket.api.infrastructure.config.HistoryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use Case for checking if price history needs to be updated
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceHistoryUseCase {
    private final SteamPriceHistoryRepository priceHistoryRepository;
    private final HistoryConfig historyConfig;

    /**
     * Checks if price history needs to be updated for a skin name and wear combination
     *
     * @param skinName The skin name
     * @param wear The wear category
     * @return true if history needs update (doesn't exist or is outdated), false otherwise
     */
    public boolean needsUpdate(String skinName, Wear wear) {
        Optional<SteamPriceHistory> latestHistory = priceHistoryRepository.findLatestBySkinNameAndWear(skinName, wear);

        // If no history exists, needs update
        if (latestHistory.isEmpty()) {
            log.debug("No price history found for {} ({}), needs update", skinName, wear);
            return true;
        }

        // Check if history is outdated
        SteamPriceHistory history = latestHistory.get();
        LocalDateTime expirationTime = history.getRecordedAt()
                .plusSeconds(historyConfig.getExpirationSeconds());

        boolean isOutdated = LocalDateTime.now().isAfter(expirationTime);

        if (isOutdated) {
            log.debug("Price history for {} ({}) is outdated (last updated: {}), needs update",
                    skinName, wear, history.getRecordedAt());
        } else {
            log.debug("Price history for {} ({}) is still valid (last updated: {})",
                    skinName, wear, history.getRecordedAt());
        }

        return isOutdated;
    }
}
