package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.repository.SteamPriceHistoryRepository;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for completing a history update task
 * Saves the price history and marks the task as completed
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteHistoryUpdateTaskUseCase {
    private final HistoryUpdateTaskRepository taskRepository;
    private final SteamPriceHistoryRepository priceHistoryRepository;

    /**
     * Completes a history update task by saving the price history and marking task as completed
     *
     * @param taskId The task ID
     * @param skinName The skin name
     * @param wear The wear category
     * @param averagePrice The average price from Steam sales (in cents)
     * @throws IllegalArgumentException if task not found or skin name/wear mismatch
     */
    @Transactional
    public void execute(Long taskId, String skinName, Wear wear, Long averagePrice) {
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

        // Save price history
        SteamPriceHistory priceHistory = SteamPriceHistory.create(
                null, // skinId is optional for now
                skinName,
                wear,
                averagePrice
        );
        priceHistoryRepository.save(priceHistory);

        log.info("Saved price history for {} ({}) - Average price: {} cents",
                skinName, wear, averagePrice);

        // Mark task as completed
        task.complete();
        taskRepository.save(task);

        log.info("Completed task ID {} for {} ({})", taskId, skinName, wear);
    }
}
