package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.repository.SkinRepository;
import com.thetruemarket.api.domain.valueobject.SkinStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use Case for marking stale skins as sold
 * Implements heartbeat-based cleanup strategy:
 * - Finds skins that are AVAILABLE but haven't been seen for configured duration
 * - Marks them as SOLD (not deleted, preserving history)
 *
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarkStaleSkinAsSoldUseCase {
    private final SkinRepository skinRepository;

    @Value("${skin.cleanup.stale-hours:2}")
    private int staleHours;

    /**
     * Finds and marks stale skins as SOLD
     * A skin is considered stale if it's AVAILABLE but hasn't been seen for configured hours
     *
     * @return Number of skins marked as sold
     */
    @Transactional
    public int execute() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(staleHours);

        log.info("Starting stale skin cleanup - marking skins not seen since {} as SOLD", cutoffDate);

        // Find all AVAILABLE skins that haven't been seen since cutoff
        List<Skin> staleSkins = skinRepository.findByStatusAndLastSeenAtBefore(
                SkinStatus.AVAILABLE,
                cutoffDate
        );

        if (staleSkins.isEmpty()) {
            log.info("No stale skins found");
            return 0;
        }

        log.info("Found {} stale skins to mark as SOLD", staleSkins.size());

        // Mark each as SOLD and save
        int markedCount = 0;
        for (Skin skin : staleSkins) {
            Skin updatedSkin = Skin.builder()
                    .id(skin.getId())
                    .name(skin.getName())
                    .assetId(skin.getAssetId())
                    .floatValue(skin.getFloatValue())
                    .wear(skin.getWear())
                    .paintSeed(skin.getPaintSeed())
                    .paintIndex(skin.getPaintIndex())
                    .stickers(skin.getStickers())
                    .stickerCount(skin.getStickerCount())
                    .price(skin.getPrice())
                    .currency(skin.getCurrency())
                    .marketSource(skin.getMarketSource())
                    .link(skin.getLink())
                    .createdAt(skin.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .lastSeenAt(skin.getLastSeenAt())
                    .status(SkinStatus.SOLD) // Mark as SOLD
                    .build();

            skinRepository.save(updatedSkin);
            markedCount++;

            log.debug("Marked skin as SOLD: {} (ID: {}, Last seen: {})",
                    skin.getName(), skin.getId(), skin.getLastSeenAt());
        }

        log.info("Successfully marked {} skins as SOLD", markedCount);
        return markedCount;
    }
}
