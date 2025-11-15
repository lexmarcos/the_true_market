package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.repository.SkinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for saving a skin to the database
 * Implements the Single Responsibility Principle (SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaveSkinUseCase {
    private final SkinRepository skinRepository;

    /**
     * Saves or updates a skin
     * Always performs save operation to update heartbeat timestamp (lastSeenAt)
     * This enables the heartbeat-based cleanup strategy for sold skins
     *
     * @param skin The skin to save or update
     * @return The saved/updated skin
     */
    @Transactional
    public Skin execute(Skin skin) {
        boolean isExisting = skinRepository.existsById(skin.getId());

        // Always save (insert or update) to refresh heartbeat timestamp
        Skin savedSkin = skinRepository.save(skin);

        if (isExisting) {
            log.debug("Updated existing skin heartbeat: {} (ID: {})",
                    savedSkin.getName(), savedSkin.getId());
        } else {
            log.info("Saved new skin: {} (ID: {}, Wear: {})",
                    savedSkin.getName(), savedSkin.getId(), savedSkin.getWear());
        }

        return savedSkin;
    }
}
