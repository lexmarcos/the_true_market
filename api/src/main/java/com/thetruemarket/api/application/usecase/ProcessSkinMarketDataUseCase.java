package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.model.SkinMarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main orchestrator Use Case for processing skin market data from RabbitMQ
 * Coordinates the workflow: save skin -> check history -> create task if needed
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessSkinMarketDataUseCase {
    private final SaveSkinUseCase saveSkinUseCase;
    private final CheckPriceHistoryUseCase checkPriceHistoryUseCase;
    private final CreateHistoryUpdateTaskUseCase createHistoryUpdateTaskUseCase;

    /**
     * Processes skin market data received from RabbitMQ
     * 1. Saves the skin if it doesn't exist
     * 2. Checks if price history needs update
     * 3. Creates a history update task if necessary
     *
     * @param skinMarketData The skin market data from RabbitMQ message
     */
    @Transactional
    public void execute(SkinMarketData skinMarketData) {
        log.info("Processing skin market data: {} (ID: {})",
                skinMarketData.getName(), skinMarketData.getId());

        try {
            // Step 1: Create domain Skin entity and save if it doesn't exist
            Skin skin = Skin.create(
                    skinMarketData.getId(),
                    skinMarketData.getName(),
                    skinMarketData.getAssetId(),
                    skinMarketData.getFloatValue(),
                    skinMarketData.getPaintSeed(),
                    skinMarketData.getPaintIndex(),
                    skinMarketData.getStickers(),
                    skinMarketData.getStickerCount()
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

            log.info("Successfully processed skin: {} ({})", skin.getName(), skin.getWear());

        } catch (Exception e) {
            log.error("Error processing skin market data for {}: {}",
                    skinMarketData.getName(), e.getMessage(), e);
            throw e;
        }
    }
}
