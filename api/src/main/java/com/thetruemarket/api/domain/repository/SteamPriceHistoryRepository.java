package com.thetruemarket.api.domain.repository;

import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.valueobject.Wear;

import java.util.Optional;

/**
 * Repository port for SteamPriceHistory domain entity
 * Interface following Dependency Inversion Principle (SOLID)
 */
public interface SteamPriceHistoryRepository {
    /**
     * Saves a price history record to the repository
     *
     * @param priceHistory The price history record to save
     * @return The saved record
     */
    SteamPriceHistory save(SteamPriceHistory priceHistory);

    /**
     * Finds the most recent price history for a specific skin name and wear combination
     *
     * @param skinName The name of the skin
     * @param wear The wear category
     * @return Optional containing the most recent price history if found
     */
    Optional<SteamPriceHistory> findLatestBySkinNameAndWear(String skinName, Wear wear);
}
