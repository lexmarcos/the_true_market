package com.thetruemarket.api.infrastructure.service;

import com.thetruemarket.api.domain.model.ProfitResult;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import com.thetruemarket.api.domain.service.ProfitCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of ProfitCalculationService
 * Calculates profit metrics considering Steam's 15% fee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfitCalculationServiceImpl implements ProfitCalculationService {
    private static final double STEAM_FEE_PERCENTAGE = 15.0;
    private final CurrencyConversionService currencyConversionService;

    @Override
    public ProfitResult calculateProfit(Long marketPrice, String currency, Long steamPrice) {
        if (marketPrice == null || marketPrice <= 0) {
            throw new IllegalArgumentException("Market price must be positive");
        }

        if (steamPrice == null || steamPrice <= 0) {
            throw new IllegalArgumentException("Steam price must be positive");
        }

        // Step 1: Convert market price to USD if necessary
        Long marketPriceUsd;
        if ("USD".equalsIgnoreCase(currency)) {
            marketPriceUsd = marketPrice;
        } else {
            // Convert from other currency to USD
            marketPriceUsd = currencyConversionService.convertBrlToUsd(marketPrice);
            log.debug("Converted market price from {} to USD: {} -> {}", currency, marketPrice, marketPriceUsd);
        }

        // Step 2: Calculate discount percentage
        // Formula: ((steamPrice - marketPrice) / steamPrice) × 100
        BigDecimal steam = BigDecimal.valueOf(steamPrice);
        BigDecimal market = BigDecimal.valueOf(marketPriceUsd);
        BigDecimal discount = steam.subtract(market)
                .divide(steam, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Convert to basis points (multiply by 100 for consistency with cents)
        // Example: 14.51% becomes 1451 basis points
        BigDecimal discountBp = discount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
        Double discountPercentage = discountBp.doubleValue();

        // Step 3: Calculate net profit percentage (discount - Steam's 15% fee)
        // Formula: (discount% - 15%) × 100 (in basis points)
        BigDecimal profitBp = discount.subtract(BigDecimal.valueOf(STEAM_FEE_PERCENTAGE))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
        Double profitPercentage = profitBp.doubleValue();

        // Step 4: Calculate expected gain in USD cents
        // Formula: (steamPrice × profitPercentage) / 10000 (divide by 10000 because profit is in basis points)
        BigDecimal expectedGain = steam
                .multiply(BigDecimal.valueOf(profitPercentage))
                .divide(BigDecimal.valueOf(10000), 0, RoundingMode.HALF_UP);

        Long expectedGainCents = expectedGain.longValue();

        log.debug("Profit calculation: market={} USD, steam={} USD, discount={} bp, profit={} bp, gain={} cents",
                marketPriceUsd, steamPrice, String.format("%.0f", discountPercentage),
                String.format("%.0f", profitPercentage), expectedGainCents);

        return ProfitResult.builder()
                .discountPercentage(discountPercentage)
                .profitPercentage(profitPercentage)
                .expectedGainCents(expectedGainCents)
                .marketPriceUsd(marketPriceUsd)
                .steamPriceUsd(steamPrice)
                .build();
    }
}
