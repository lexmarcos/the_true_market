package com.thetruemarket.api.domain.valueobject;

import lombok.Getter;

/**
 * CS2 Skin Wear Categories based on float value
 * Represents the condition quality of a skin
 */
@Getter
public enum Wear {
    FACTORY_NEW("Factory New", 0.00, 0.07),
    MINIMAL_WEAR("Minimal Wear", 0.07, 0.15),
    FIELD_TESTED("Field-Tested", 0.15, 0.38),
    WELL_WORN("Well-Worn", 0.38, 0.45),
    BATTLE_SCARRED("Battle-Scarred", 0.45, 1.00);

    private final String displayName;
    private final double minFloat;
    private final double maxFloat;

    Wear(String displayName, double minFloat, double maxFloat) {
        this.displayName = displayName;
        this.minFloat = minFloat;
        this.maxFloat = maxFloat;
    }

    /**
     * Determines the wear category from a float value
     *
     * @param floatValue The float value of the skin (0.00 - 1.00)
     * @return The corresponding Wear category
     * @throws IllegalArgumentException if float value is out of valid range
     */
    public static Wear fromFloatValue(Double floatValue) {
        if (floatValue == null) {
            throw new IllegalArgumentException("Float value cannot be null");
        }

        if (floatValue < 0.0 || floatValue > 1.0) {
            throw new IllegalArgumentException("Float value must be between 0.0 and 1.0, got: " + floatValue);
        }

        for (Wear wear : values()) {
            if (floatValue >= wear.minFloat && floatValue < wear.maxFloat) {
                return wear;
            }
        }

        // Edge case: exactly 1.0 should be Battle-Scarred
        if (floatValue == 1.0) {
            return BATTLE_SCARRED;
        }

        throw new IllegalStateException("Unable to determine wear for float value: " + floatValue);
    }

    /**
     * Extracts wear category from skin name
     * Looks for wear text in parentheses, e.g., "AK-47 | Redline (Field-Tested)"
     *
     * @param skinName The full skin name with wear in parentheses
     * @return The corresponding Wear category
     * @throws IllegalArgumentException if wear cannot be extracted or mapped
     */
    public static Wear fromSkinName(String skinName) {
        if (skinName == null || skinName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skin name cannot be null or empty");
        }

        // Extract text between parentheses
        int openParen = skinName.lastIndexOf('(');
        int closeParen = skinName.lastIndexOf(')');

        if (openParen == -1 || closeParen == -1 || openParen >= closeParen) {
            throw new IllegalArgumentException(
                    "Cannot extract wear from skin name: " + skinName + " (no parentheses found)"
            );
        }

        String wearText = skinName.substring(openParen + 1, closeParen).trim();

        // Map wear text to enum
        return fromDisplayName(wearText);
    }

    /**
     * Maps display name text to Wear enum
     * Case-insensitive matching
     *
     * @param displayName The wear display name (e.g., "Factory New", "Field-Tested")
     * @return The corresponding Wear category
     * @throws IllegalArgumentException if display name doesn't match any wear
     */
    public static Wear fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }

        String normalized = displayName.trim().toLowerCase().replace("-", " ");

        for (Wear wear : values()) {
            String wearNormalized = wear.displayName.toLowerCase().replace("-", " ");
            if (wearNormalized.equals(normalized)) {
                return wear;
            }
        }

        throw new IllegalArgumentException(
                "Unknown wear display name: " + displayName +
                ". Valid values: Factory New, Minimal Wear, Field-Tested, Well-Worn, Battle-Scarred"
        );
    }
}
