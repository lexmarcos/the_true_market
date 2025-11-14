package com.thetruemarket.api.infrastructure.job;

import com.thetruemarket.api.application.usecase.CheckPriceHistoryUseCase;
import com.thetruemarket.api.application.usecase.CreateHistoryUpdateTaskUseCase;
import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.repository.SkinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled job to update Steam prices for existing skins
 * Processes skins in batches to avoid memory issues and creates update tasks
 * with deduplication
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "skin.price.update.enabled", havingValue = "true", matchIfMissing = true)
public class UpdateSkinPricesJob {
  private final SkinRepository skinRepository;
  private final CheckPriceHistoryUseCase checkPriceHistoryUseCase;
  private final CreateHistoryUpdateTaskUseCase createHistoryUpdateTaskUseCase;

  @Value("${skin.price.update.batch-size:100}")
  private int batchSize;

  // Track the current page to process across job executions
  private final AtomicInteger currentPage = new AtomicInteger(0);

  /**
   * Runs every 30 minutes to update prices for a batch of skins
   */
  @Scheduled(fixedRateString = "${skin.price.update.interval-ms:1800000}") // Default: 30 minutes
  public void updateSkinPrices() {
    log.info("Starting UpdateSkinPricesJob (batch size: {})", batchSize);

    try {
      int pageNumber = currentPage.get();
      Pageable pageable = PageRequest.of(pageNumber, batchSize);

      // Fetch one page of skins
      Page<Skin> skinPage = skinRepository.findAll(pageable);

      if (skinPage.isEmpty()) {
        log.debug("No skins found on page {}, resetting to page 0", pageNumber);
        currentPage.set(0);
        return;
      }

      log.info("Processing page {} of {} (total elements: {}, batch size: {})",
          pageNumber + 1,
          skinPage.getTotalPages(),
          skinPage.getTotalElements(),
          skinPage.getNumberOfElements());

      // Group skins by unique (skinName, wear) combination to avoid duplicate tasks
      Map<String, Skin> uniqueSkins = new HashMap<>();
      for (Skin skin : skinPage.getContent()) {
        String key = skin.getName() + "|" + skin.getWear();
        // Keep first occurrence of each unique combination
        uniqueSkins.putIfAbsent(key, skin);
      }

      log.debug("Found {} unique skin combinations in current batch", uniqueSkins.size());

      int tasksCreated = 0;
      int skippedNoUpdate = 0;
      int skippedDuplicate = 0;

      // Process each unique skin
      for (Skin skin : uniqueSkins.values()) {
        try {
          // Check if price history needs update
          boolean needsUpdate = checkPriceHistoryUseCase.needsUpdate(
              skin.getName(),
              skin.getWear());

          if (!needsUpdate) {
            skippedNoUpdate++;
            log.debug("Skipping {} ({}), price history still valid",
                skin.getName(), skin.getWear());
            continue;
          }

          // Attempt to create update task (will return null if waiting task already
          // exists)
          var createdTask = createHistoryUpdateTaskUseCase.execute(
              skin.getName(),
              skin.getWear());

          if (createdTask != null) {
            tasksCreated++;
            log.debug("Created update task for {} ({})", skin.getName(), skin.getWear());
          } else {
            skippedDuplicate++;
            log.debug("Skipped {} ({}), waiting task already exists",
                skin.getName(), skin.getWear());
          }

        } catch (Exception e) {
          log.error("Error processing skin {} ({}): {}",
              skin.getName(), skin.getWear(), e.getMessage(), e);
        }
      }

      // Move to next page for next execution (wrap around if at the end)
      int nextPage = pageNumber + 1;
      if (nextPage >= skinPage.getTotalPages()) {
        log.info("Reached last page, resetting to page 0 for next execution");
        currentPage.set(0);
      } else {
        currentPage.set(nextPage);
      }

      log.info(
          "UpdateSkinPricesJob completed: {} tasks created, {} skipped (no update needed), {} skipped (duplicate waiting task), page {}/{}",
          tasksCreated,
          skippedNoUpdate,
          skippedDuplicate,
          pageNumber + 1,
          skinPage.getTotalPages());

    } catch (Exception e) {
      log.error("Error in UpdateSkinPricesJob: {}", e.getMessage(), e);
    }
  }
}
