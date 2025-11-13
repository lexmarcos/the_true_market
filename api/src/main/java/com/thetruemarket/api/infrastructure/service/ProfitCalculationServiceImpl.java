package com.thetruemarket.api.infrastructure.service;

import com.thetruemarket.api.domain.model.ProfitResult;
import com.thetruemarket.api.domain.service.ProfitCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of ProfitCalculationService
 * Calculates profit metrics considering Steam's 15% fee
 * All prices are expected to be in USD cents
 */
@Service
@Slf4j
public class ProfitCalculationServiceImpl implements ProfitCalculationService {
    private static final double STEAM_FEE_PERCENTAGE = 15.0;

    @Override
    public ProfitResult calculateProfit(Long marketPriceUsd, Long steamPriceUsd) {
        if (marketPriceUsd == null || marketPriceUsd <= 0) {
            throw new IllegalArgumentException("Market price must be positive");
        }

        if (steamPriceUsd == null || steamPriceUsd <= 0) {
            throw new IllegalArgumentException("Steam price must be positive");
        }

        // Step 1: Calculate discount percentage
        // Formula: ((steamPrice - marketPrice) / steamPrice) × 100
        BigDecimal steam = BigDecimal.valueOf(steamPriceUsd);
        BigDecimal market = BigDecimal.valueOf(marketPriceUsd);
        BigDecimal discount = steam.subtract(market)
                .divide(steam, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Convert to basis points (multiply by 100 for consistency with cents)
        // Example: 14.51% becomes 1451 basis points
        BigDecimal discountBp = discount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
        Double discountPercentage = discountBp.doubleValue();

        // Step 2: Calculate net profit percentage (discount - Steam's 15% fee)
        // Formula: (discount% - 15%) × 100 (in basis points)
        BigDecimal profitBp = discount.subtract(BigDecimal.valueOf(STEAM_FEE_PERCENTAGE))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
        Double profitPercentage = profitBp.doubleValue();

        // Step 3: Calculate expected gain in USD cents
        // Formula: (steamPrice × profitPercentage) / 10000 (divide by 10000 because profit is in basis points)
        BigDecimal expectedGain = steam
                .multiply(BigDecimal.valueOf(profitPercentage))
                .divide(BigDecimal.valueOf(10000), 0, RoundingMode.HALF_UP);

        Long expectedGainCents = expectedGain.longValue();

        log.debug("Profit calculation: market={} USD, steam={} USD, discount={} bp, profit={} bp, gain={} cents",
                marketPriceUsd, steamPriceUsd, String.format("%.0f", discountPercentage),
                String.format("%.0f", profitPercentage), expectedGainCents);

        return ProfitResult.builder()
                .discountPercentage(discountPercentage)
                .profitPercentage(profitPercentage)
                .expectedGainCents(expectedGainCents)
                .marketPriceUsd(marketPriceUsd)
                .steamPriceUsd(steamPriceUsd)
                .build();
    }
}
