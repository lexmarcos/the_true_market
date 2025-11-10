package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for creating a history update task
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateHistoryUpdateTaskUseCase {
    private final HistoryUpdateTaskRepository taskRepository;

    /**
     * Creates a new history update task if one doesn't already exist in WAITING status
     *
     * @param skinName The skin name
     * @param wear The wear category
     * @return The created task, or null if a waiting task already exists
     */
    @Transactional
    public HistoryUpdateTask execute(String skinName, Wear wear) {
        // Check if a waiting task already exists for this skin + wear combination
        boolean alreadyExists = taskRepository.existsBySkinNameAndWearAndStatus(
                skinName, wear, TaskStatus.WAITING
        );

        if (alreadyExists) {
            log.debug("A waiting task already exists for {} ({}), skipping creation", skinName, wear);
            return null;
        }

        // Create new waiting task
        HistoryUpdateTask task = HistoryUpdateTask.createWaiting(skinName, wear);
        HistoryUpdateTask savedTask = taskRepository.save(task);

        log.info("Created history update task for {} ({}) - Task ID: {}",
                skinName, wear, savedTask.getId());

        return savedTask;
    }
}
