package com.thetruemarket.api.domain.repository;

import com.thetruemarket.api.domain.model.Skin;

import java.util.Optional;

/**
 * Repository port for Skin domain entity
 * Interface following Dependency Inversion Principle (SOLID)
 */
public interface SkinRepository {
    /**
     * Saves a skin to the repository
     *
     * @param skin The skin to save
     * @return The saved skin
     */
    Skin save(Skin skin);

    /**
     * Finds a skin by its unique identifier
     *
     * @param id The skin ID
     * @return Optional containing the skin if found
     */
    Optional<Skin> findById(String id);

    /**
     * Checks if a skin exists by its ID
     *
     * @param id The skin ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
}
