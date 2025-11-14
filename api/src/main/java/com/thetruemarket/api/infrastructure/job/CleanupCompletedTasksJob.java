package com.thetruemarket.api.infrastructure.job;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to clean up completed history update tasks
 * Deletes tasks that have been completed for more than 24 hours
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupCompletedTasksJob {
  private final HistoryUpdateTaskRepository historyUpdateTaskRepository;

  private static final int RETENTION_HOURS = 24;

  /**
   * Runs every 30 minutes to clean up old completed tasks
   */
  @Scheduled(fixedRateString = "${task.cleanup.interval-ms:1800000}") // Default: 30 minutes
  @Transactional
  public void cleanupCompletedTasks() {
    log.info("Starting CleanupCompletedTasksJob");

    try {
      // Calculate the cutoff date (24 hours ago)
      LocalDateTime cutoffDate = LocalDateTime.now().minusHours(RETENTION_HOURS);

      log.debug("Looking for completed tasks finished before: {}", cutoffDate);

      // Find tasks to delete
      List<HistoryUpdateTask> tasksToDelete = historyUpdateTaskRepository
          .findByStatusAndFinishedAtBefore(TaskStatus.COMPLETED, cutoffDate);

      if (tasksToDelete.isEmpty()) {
        log.debug("No completed tasks found older than {} hours", RETENTION_HOURS);
        return;
      }

      log.info("Found {} completed tasks older than {} hours", tasksToDelete.size(), RETENTION_HOURS);

      // Delete tasks
      historyUpdateTaskRepository.deleteByStatusAndFinishedAtBefore(TaskStatus.COMPLETED, cutoffDate);

      log.info("CleanupCompletedTasksJob completed: {} tasks deleted", tasksToDelete.size());

    } catch (Exception e) {
      log.error("Error in CleanupCompletedTasksJob: {}", e.getMessage(), e);
    }
  }
}
