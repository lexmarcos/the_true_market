package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.repository.SteamPriceHistoryRepository;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for completing a history update task
 * Saves the price history and marks the task as completed
 * Converts price from BRL to USD before saving
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteHistoryUpdateTaskUseCase {
    private final HistoryUpdateTaskRepository taskRepository;
    private final SteamPriceHistoryRepository priceHistoryRepository;
    private final CurrencyConversionService currencyConversionService;

    /**
     * Completes a history update task by saving the price history and marking task as completed
     * Converts prices from BRL to USD where needed
     *
     * @param taskId The task ID
     * @param skinName The skin name
     * @param wear The wear category
     * @param averagePriceInBrl The average price from Steam sales in BRL (cents)
     * @param lastSalePriceInBrl The price of the last sale in BRL (cents)
     * @param lowestBuyOrderPriceInUsd The price of the lowest buy order in USD (cents)
     * @throws IllegalArgumentException if task not found or skin name/wear mismatch
     */
    @Transactional
    public void execute(Long taskId, String skinName, Wear wear, Long averagePriceInBrl,
                       Long lastSalePriceInBrl, Long lowestBuyOrderPriceInUsd) {
        // Find the task
        HistoryUpdateTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // Validate that skin name and wear match
        if (!task.getSkinName().equals(skinName)) {
            throw new IllegalArgumentException(
                    String.format("Skin name mismatch. Task has '%s' but received '%s'",
                            task.getSkinName(), skinName)
            );
        }

        if (!task.getWear().equals(wear)) {
            throw new IllegalArgumentException(
                    String.format("Wear mismatch. Task has '%s' but received '%s'",
                            task.getWear(), wear)
            );
        }

        // Convert prices from BRL to USD
        Long averagePriceInUsd = currencyConversionService.convertBrlToUsd(averagePriceInBrl);
        Long lastSalePriceInUsd = currencyConversionService.convertBrlToUsd(lastSalePriceInBrl);
        // lowestBuyOrderPriceInUsd already comes in USD, no conversion needed

        log.info("Converted prices for {} ({}): avgPrice {} BRL -> {} USD, lastSale {} BRL -> {} USD, lowestBuyOrder {} USD",
                skinName, wear, averagePriceInBrl, averagePriceInUsd, lastSalePriceInBrl, lastSalePriceInUsd, lowestBuyOrderPriceInUsd);

        // Save price history (all prices in USD)
        SteamPriceHistory priceHistory = SteamPriceHistory.create(
                null, // skinId is optional for now
                skinName,
                wear,
                averagePriceInUsd,
                lastSalePriceInUsd,
                lowestBuyOrderPriceInUsd
        );
        priceHistoryRepository.save(priceHistory);

        log.info("Saved price history for {} ({}) - Avg: {} USD, LastSale: {} USD, LowestBuyOrder: {} USD",
                skinName, wear, averagePriceInUsd, lastSalePriceInUsd, lowestBuyOrderPriceInUsd);

        // Mark task as completed
        task.complete();
        taskRepository.save(task);

        log.info("Completed task ID {} for {} ({})", taskId, skinName, wear);
    }
}
