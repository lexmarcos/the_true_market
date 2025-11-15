package com.thetruemarket.api.domain.valueobject;

/**
 * Represents the availability status of a skin in the marketplace
 * Used for heartbeat-based tracking to identify sold skins
 */
public enum SkinStatus {
    /**
     * Skin is currently available for purchase
     * Bot is actively seeing this skin in the marketplace
     */
    AVAILABLE,

    /**
     * Skin has been sold
     * Marked when skin hasn't been seen by bot for configured duration (default: 2 hours)
     */
    SOLD,

    /**
     * Skin is temporarily unavailable
     * Reserved for future use (e.g., marketplace maintenance, errors)
     */
    UNAVAILABLE
}
