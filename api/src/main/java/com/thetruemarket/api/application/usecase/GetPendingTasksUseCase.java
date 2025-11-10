package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case for retrieving pending history update tasks
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetPendingTasksUseCase {
    private final HistoryUpdateTaskRepository taskRepository;

    /**
     * Retrieves all pending (WAITING) tasks ordered by creation date (FIFO)
     *
     * @return List of waiting tasks in FIFO order
     */
    public List<HistoryUpdateTask> execute() {
        List<HistoryUpdateTask> waitingTasks = taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.WAITING);

        log.info("Retrieved {} waiting tasks", waitingTasks.size());

        return waitingTasks;
    }
}
