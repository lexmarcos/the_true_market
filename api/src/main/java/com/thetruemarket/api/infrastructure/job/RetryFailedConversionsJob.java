package com.thetruemarket.api.infrastructure.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetruemarket.api.domain.exception.ExchangeRateUnavailableException;
import com.thetruemarket.api.domain.model.FailedConversionTask;
import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.model.SkinMarketData;
import com.thetruemarket.api.domain.repository.FailedConversionTaskRepository;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import com.thetruemarket.api.application.usecase.SaveSkinUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to retry failed currency conversions
 * Runs periodically to attempt conversion of previously failed tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryFailedConversionsJob {
    private final FailedConversionTaskRepository failedConversionTaskRepository;
    private final CurrencyConversionService currencyConversionService;
    private final SaveSkinUseCase saveSkinUseCase;
    private final ObjectMapper objectMapper;

    @Value("${exchange-rate.retry.max-attempts:10}")
    private int maxAttempts;

    @Value("${exchange-rate.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    /**
     * Runs every hour to retry failed conversions
     */
    @Scheduled(fixedRateString = "${exchange-rate.retry.interval-ms:3600000}") // Default: 1 hour
    public void retryFailedConversions() {
        log.info("Starting RetryFailedConversionsJob");

        try {
            // Find all tasks ready for retry
            List<FailedConversionTask> tasksToRetry =
                    failedConversionTaskRepository.findTasksReadyForRetry(LocalDateTime.now());

            if (tasksToRetry.isEmpty()) {
                log.debug("No failed conversion tasks ready for retry");
                return;
            }

            log.info("Found {} tasks ready for retry", tasksToRetry.size());

            int successCount = 0;
            int failCount = 0;
            int permanentlyFailedCount = 0;

            for (FailedConversionTask task : tasksToRetry) {
                try {
                    boolean success = retryConversion(task);
                    if (success) {
                        successCount++;
                        // Delete the task since it succeeded
                        failedConversionTaskRepository.deleteById(task.getId());
                        log.info("Successfully processed and deleted task {}", task.getId());
                    } else {
                        // Update task with new attempt info
                        failedConversionTaskRepository.save(task);

                        if (task.getPermanentlyFailed()) {
                            permanentlyFailedCount++;
                            log.error("Task {} marked as permanently failed after {} attempts",
                                    task.getId(), task.getAttemptCount());
                        } else {
                            failCount++;
                            log.warn("Task {} failed, will retry at {}",
                                    task.getId(), task.getNextRetryAt());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing task {}: {}", task.getId(), e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("RetryFailedConversionsJob completed: {} succeeded, {} failed, {} permanently failed",
                    successCount, failCount, permanentlyFailedCount);

        } catch (Exception e) {
            log.error("Error in RetryFailedConversionsJob: {}", e.getMessage(), e);
        }
    }

    /**
     * Attempts to retry a single failed conversion
     *
     * @param task The task to retry
     * @return true if successful, false if failed
     */
    private boolean retryConversion(FailedConversionTask task) {
        log.debug("Retrying conversion for task {} (attempt {})", task.getId(), task.getAttemptCount() + 1);

        try {
            // Deserialize SkinMarketData from JSON
            SkinMarketData skinMarketData = objectMapper.readValue(
                    task.getSkinDataJson(),
                    SkinMarketData.class
            );

            // Try to convert price to USD
            Long priceInUsd;
            try {
                priceInUsd = currencyConversionService.convertBrlToUsd(skinMarketData.getPrice());
                log.info("Successfully converted price for task {}: {} {} -> {} USD",
                        task.getId(), skinMarketData.getPrice(), task.getCurrency(), priceInUsd);
            } catch (ExchangeRateUnavailableException e) {
                // Conversion still failing
                log.warn("Conversion still unavailable for task {}: {}", task.getId(), e.getMessage());
                task.incrementAttempt(e.getMessage(), initialDelayMinutes, maxAttempts);
                return false;
            }

            // Create and save Skin with USD price
            Skin skin = Skin.create(
                    skinMarketData.getId(),
                    skinMarketData.getName(),
                    skinMarketData.getAssetId(),
                    skinMarketData.getFloatValue(),
                    skinMarketData.getPaintSeed(),
                    skinMarketData.getPaintIndex(),
                    skinMarketData.getStickers(),
                    skinMarketData.getStickerCount(),
                    priceInUsd,
                    "USD",
                    skinMarketData.getStore(),
                    skinMarketData.getLink(),
                    skinMarketData.getImageUrl()
            );

            saveSkinUseCase.execute(skin);
            log.info("Successfully saved skin {} from retry task {}", skin.getId(), task.getId());

            return true;

        } catch (Exception e) {
            log.error("Error retrying conversion for task {}: {}", task.getId(), e.getMessage(), e);
            task.incrementAttempt(e.getMessage(), initialDelayMinutes, maxAttempts);
            return false;
        }
    }
}
