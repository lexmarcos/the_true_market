package com.thetruemarket.api.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetruemarket.api.domain.model.FailedConversionTask;
import com.thetruemarket.api.domain.model.SkinMarketData;
import com.thetruemarket.api.domain.repository.FailedConversionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Use case for saving failed currency conversion attempts
 * Stores the original data so it can be retried later
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaveFailedConversionUseCase {
    private final FailedConversionTaskRepository failedConversionTaskRepository;
    private final ObjectMapper objectMapper;

    @Value("${exchange-rate.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    /**
     * Saves a failed conversion task for later retry
     *
     * @param skinMarketData Original skin market data that failed to convert
     * @param errorMessage Error message from the failed conversion
     */
    public void execute(SkinMarketData skinMarketData, String errorMessage) {
        try {
            // Convert SkinMarketData to JSON for storage
            String skinDataJson = objectMapper.writeValueAsString(skinMarketData);

            // Create failed conversion task
            FailedConversionTask task = FailedConversionTask.create(
                    skinDataJson,
                    skinMarketData.getPrice(),
                    skinMarketData.getCurrency(),
                    skinMarketData.getId(),
                    errorMessage,
                    initialDelayMinutes
            );

            // Save to repository
            FailedConversionTask saved = failedConversionTaskRepository.save(task);

            log.warn("Saved failed conversion task for skin {} (ID: {}). Will retry at: {}",
                    skinMarketData.getName(),
                    skinMarketData.getId(),
                    saved.getNextRetryAt());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SkinMarketData to JSON for skin {}: {}",
                    skinMarketData.getId(), e.getMessage(), e);
            // Don't throw - we don't want to lose the original message
        } catch (Exception e) {
            log.error("Failed to save failed conversion task for skin {}: {}",
                    skinMarketData.getId(), e.getMessage(), e);
            // Don't throw - we don't want to lose the original message
        }
    }
}
