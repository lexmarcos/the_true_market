package com.thetruemarket.api.domain.exception;

/**
 * Exception thrown when a skin image URL cannot be resolved.
 * This can occur when:
 * - Steam API calls fail
 * - Invalid class ID or icon URL provided
 * - API returns unexpected data structure
 * Domain exception following Clean Architecture principles.
 */
public class SkinImageResolutionException extends RuntimeException {

    public SkinImageResolutionException(String message) {
        super(message);
    }

    public SkinImageResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
