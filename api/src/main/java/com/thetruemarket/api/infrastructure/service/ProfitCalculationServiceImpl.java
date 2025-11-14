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
    public ProfitResult calculateProfit(Long marketPriceUsd, Long steamPriceUsd, Long lastSalePrice, Long lowestBuyOrderPrice) {
        if (marketPriceUsd == null || marketPriceUsd <= 0) {
            throw new IllegalArgumentException("Market price must be positive");
        }

        if (steamPriceUsd == null || steamPriceUsd <= 0) {
            throw new IllegalArgumentException("Steam price must be positive");
        }

        BigDecimal market = BigDecimal.valueOf(marketPriceUsd);

        // Step 1: Calculate discount and profit percentage based on Steam average price
        BigDecimal steam = BigDecimal.valueOf(steamPriceUsd);
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

        // Step 4: Calculate profit percentage vs last sale price (if available)
        Double profitPercentageVsLastSale = null;
        if (lastSalePrice != null && lastSalePrice > 0) {
            profitPercentageVsLastSale = calculateProfitPercentage(market, BigDecimal.valueOf(lastSalePrice));
        }

        // Step 5: Calculate profit percentage vs lowest buy order price (if available)
        Double profitPercentageVsLowestBuyOrder = null;
        if (lowestBuyOrderPrice != null && lowestBuyOrderPrice > 0) {
            profitPercentageVsLowestBuyOrder = calculateProfitPercentage(market, BigDecimal.valueOf(lowestBuyOrderPrice));
        }

        log.debug("Profit calculation: market={} USD, steam={} USD, discount={} bp, profit={} bp, gain={} cents, " +
                        "profitVsLastSale={} bp, profitVsLowestBuyOrder={} bp",
                marketPriceUsd, steamPriceUsd, String.format("%.0f", discountPercentage),
                String.format("%.0f", profitPercentage), expectedGainCents,
                profitPercentageVsLastSale != null ? String.format("%.0f", profitPercentageVsLastSale) : "N/A",
                profitPercentageVsLowestBuyOrder != null ? String.format("%.0f", profitPercentageVsLowestBuyOrder) : "N/A");

        return ProfitResult.builder()
                .discountPercentage(discountPercentage)
                .profitPercentage(profitPercentage)
                .expectedGainCents(expectedGainCents)
                .marketPriceUsd(marketPriceUsd)
                .steamPriceUsd(steamPriceUsd)
                .lastSalePrice(lastSalePrice)
                .lowestBuyOrderPrice(lowestBuyOrderPrice)
                .profitPercentageVsLastSale(profitPercentageVsLastSale)
                .profitPercentageVsLowestBuyOrder(profitPercentageVsLowestBuyOrder)
                .build();
    }

    /**
     * Calculates profit percentage for a given reference price
     * Formula: ((referencePrice - marketPrice) / referencePrice - 15%) × 100 × 100 (in basis points)
     *
     * @param marketPrice Market price in USD cents
     * @param referencePrice Reference price (last sale or lowest buy order) in USD cents
     * @return Profit percentage in basis points
     */
    private Double calculateProfitPercentage(BigDecimal marketPrice, BigDecimal referencePrice) {
        // Calculate discount percentage
        BigDecimal discount = referencePrice.subtract(marketPrice)
                .divide(referencePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Calculate net profit (discount - Steam's 15% fee)
        BigDecimal profitBp = discount.subtract(BigDecimal.valueOf(STEAM_FEE_PERCENTAGE))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);

        return profitBp.doubleValue();
    }
}
