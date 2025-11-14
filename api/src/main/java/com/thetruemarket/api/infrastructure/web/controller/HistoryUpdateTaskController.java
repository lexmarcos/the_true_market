package com.thetruemarket.api.infrastructure.web.controller;

import com.thetruemarket.api.application.usecase.CompleteHistoryUpdateTaskUseCase;
import com.thetruemarket.api.application.usecase.GetPendingTasksUseCase;
import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.infrastructure.web.dto.CompleteHistoryUpdateRequest;
import com.thetruemarket.api.infrastructure.web.dto.CompleteHistoryUpdateResponse;
import com.thetruemarket.api.infrastructure.web.dto.HistoryUpdateTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing history update tasks
 * Exposes endpoints for retrieving pending tasks and completing them
 */
@RestController
@RequestMapping("/api/v1/history-update-tasks")
@RequiredArgsConstructor
@Slf4j
public class HistoryUpdateTaskController {
    private final GetPendingTasksUseCase getPendingTasksUseCase;
    private final CompleteHistoryUpdateTaskUseCase completeHistoryUpdateTaskUseCase;

    /**
     * GET /api/v1/history-update-tasks
     * Retrieves all pending (WAITING) history update tasks ordered by creation date (FIFO)
     *
     * @return List of waiting tasks
     */
    @GetMapping
    public ResponseEntity<List<HistoryUpdateTaskResponse>> getPendingTasks() {
        log.info("GET /api/v1/history-update-tasks - Retrieving pending tasks");

        List<HistoryUpdateTask> tasks = getPendingTasksUseCase.execute();

        List<HistoryUpdateTaskResponse> response = tasks.stream()
                .map(HistoryUpdateTaskResponse::fromDomain)
                .collect(Collectors.toList());

        log.info("Returning {} pending tasks", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/history-update-tasks/{taskId}/complete
     * Completes a history update task by providing the Steam price history data
     *
     * @param taskId The task ID to complete
     * @param request The request body containing skin name, wear, prices (average, last sale, lowest buy order)
     * @return Success response
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<CompleteHistoryUpdateResponse> completeTask(
            @PathVariable Long taskId,
            @RequestBody CompleteHistoryUpdateRequest request
    ) {
        log.info("POST /api/v1/history-update-tasks/{}/complete - Completing task", taskId);

        try {
            completeHistoryUpdateTaskUseCase.execute(
                    taskId,
                    request.getSkinName(),
                    request.getWear(),
                    request.getAveragePrice(),
                    request.getLastSalePrice(),
                    request.getLowestBuyOrderPrice()
            );

            CompleteHistoryUpdateResponse response = CompleteHistoryUpdateResponse.success(
                    taskId,
                    request.getSkinName(),
                    request.getWear().getDisplayName()
            );

            log.info("Task {} completed successfully", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CompleteHistoryUpdateResponse.builder()
                            .message("Error: " + e.getMessage())
                            .taskId(taskId)
                            .build());

        } catch (Exception e) {
            log.error("Error completing task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CompleteHistoryUpdateResponse.builder()
                            .message("Internal server error: " + e.getMessage())
                            .taskId(taskId)
                            .build());
        }
    }
}
