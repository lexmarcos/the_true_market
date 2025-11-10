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
     * Saves a skin if it doesn't already exist
     *
     * @param skin The skin to save
     * @return The saved skin, or null if it already existed
     */
    @Transactional
    public Skin execute(Skin skin) {
        // Check if skin already exists by ID
        if (skinRepository.existsById(skin.getId())) {
            log.debug("Skin with ID {} already exists, skipping save", skin.getId());
            return null;
        }

        // Save the skin
        Skin savedSkin = skinRepository.save(skin);
        log.info("Saved new skin: {} (ID: {}, Wear: {})",
                savedSkin.getName(), savedSkin.getId(), savedSkin.getWear());

        return savedSkin;
    }
}
