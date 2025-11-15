package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.exception.ExchangeRateUnavailableException;
import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.model.SkinMarketData;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main orchestrator Use Case for processing skin market data from RabbitMQ
 * Coordinates the workflow: convert currency -> save skin -> check history -> create task if needed
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessSkinMarketDataUseCase {
    private final SaveSkinUseCase saveSkinUseCase;
    private final CheckPriceHistoryUseCase checkPriceHistoryUseCase;
    private final CreateHistoryUpdateTaskUseCase createHistoryUpdateTaskUseCase;
    private final CurrencyConversionService currencyConversionService;
    private final SaveFailedConversionUseCase saveFailedConversionUseCase;

    /**
     * Processes skin market data received from RabbitMQ
     * 1. Converts price to USD if necessary
     * 2. Saves the skin if it doesn't exist
     * 3. Checks if price history needs update
     * 4. Creates a history update task if necessary
     *
     * @param skinMarketData The skin market data from RabbitMQ message
     */
    @Transactional
    public void execute(SkinMarketData skinMarketData) {
        log.info("Processing skin market data: {} (ID: {}) - Price: {} {}",
                skinMarketData.getName(), skinMarketData.getId(),
                skinMarketData.getPrice(), skinMarketData.getCurrency());

        try {
            // Step 0: Convert price to USD if necessary
            Long priceInUsd;
            String finalCurrency;

            if ("USD".equalsIgnoreCase(skinMarketData.getCurrency())) {
                // Already in USD
                priceInUsd = skinMarketData.getPrice();
                finalCurrency = "USD";
                log.debug("Price already in USD: {}", priceInUsd);
            } else {
                // Need to convert from BRL (or other currency) to USD
                try {
                    priceInUsd = currencyConversionService.convertBrlToUsd(skinMarketData.getPrice());
                    finalCurrency = "USD";
                    log.info("Converted price from {} {} to {} USD",
                            skinMarketData.getPrice(), skinMarketData.getCurrency(), priceInUsd);
                } catch (ExchangeRateUnavailableException e) {
                    // Conversion failed - save to failed conversion tasks
                    log.warn("Currency conversion failed for skin {}: {}",
                            skinMarketData.getId(), e.getMessage());

                    saveFailedConversionUseCase.execute(skinMarketData, e.getMessage());

                    // Re-throw to trigger RabbitMQ retry
                    throw new ExchangeRateUnavailableException(
                            "Failed to convert price to USD for skin: " + skinMarketData.getName(), e);
                }
            }

            // Step 1: Create domain Skin entity with USD price
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
                    finalCurrency,
                    skinMarketData.getStore(),
                    skinMarketData.getLink(),
                    skinMarketData.getImageUrl()
            );

            saveSkinUseCase.execute(skin);

            // Step 2: Check if price history needs update
            boolean needsUpdate = checkPriceHistoryUseCase.needsUpdate(
                    skin.getName(),
                    skin.getWear()
            );

            // Step 3: Create history update task if needed
            if (needsUpdate) {
                createHistoryUpdateTaskUseCase.execute(
                        skin.getName(),
                        skin.getWear()
                );
            }

            log.info("Successfully processed skin: {} ({}) - Final price: {} USD",
                    skin.getName(), skin.getWear(), priceInUsd);

        } catch (ExchangeRateUnavailableException e) {
            // Re-throw exchange rate exceptions to trigger RabbitMQ retry
            throw e;
        } catch (Exception e) {
            log.error("Error processing skin market data for {}: {}",
                    skinMarketData.getName(), e.getMessage(), e);
            throw e;
        }
    }
}
