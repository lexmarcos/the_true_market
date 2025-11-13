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
     * Converts price from BRL to USD before saving
     *
     * @param taskId The task ID
     * @param skinName The skin name
     * @param wear The wear category
     * @param averagePriceInBrl The average price from Steam sales in BRL (cents)
     * @throws IllegalArgumentException if task not found or skin name/wear mismatch
     */
    @Transactional
    public void execute(Long taskId, String skinName, Wear wear, Long averagePriceInBrl) {
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

        // Convert price from BRL to USD
        Long averagePriceInUsd = currencyConversionService.convertBrlToUsd(averagePriceInBrl);

        log.info("Converted price for {} ({}): {} BRL cents -> {} USD cents",
                skinName, wear, averagePriceInBrl, averagePriceInUsd);

        // Save price history (in USD)
        SteamPriceHistory priceHistory = SteamPriceHistory.create(
                null, // skinId is optional for now
                skinName,
                wear,
                averagePriceInUsd
        );
        priceHistoryRepository.save(priceHistory);

        log.info("Saved price history for {} ({}) - Average price: {} USD cents (original: {} BRL cents)",
                skinName, wear, averagePriceInUsd, averagePriceInBrl);

        // Mark task as completed
        task.complete();
        taskRepository.save(task);

        log.info("Completed task ID {} for {} ({})", taskId, skinName, wear);
    }
}
