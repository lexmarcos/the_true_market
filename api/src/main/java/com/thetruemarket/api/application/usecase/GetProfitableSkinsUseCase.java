package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.application.dto.ProfitAnalysis;
import com.thetruemarket.api.domain.model.ProfitResult;
import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.repository.SkinRepository;
import com.thetruemarket.api.domain.repository.SteamPriceHistoryRepository;
import com.thetruemarket.api.domain.service.ProfitCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for retrieving profitable skins with profit analysis
 * Analyzes all skins against Steam price history to calculate potential profit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetProfitableSkinsUseCase {
    private final SkinRepository skinRepository;
    private final SteamPriceHistoryRepository priceHistoryRepository;
    private final ProfitCalculationService profitCalculationService;

    /**
     * Executes the use case to retrieve profitable skins
     *
     * @param minProfit Minimum profit percentage to filter (optional)
     * @param maxResults Maximum number of results to return (optional)
     * @param sortBy Field to sort by: "profit", "discount", "gain" (optional, defaults to "profit")
     * @param order Sort order: "asc" or "desc" (optional, defaults to "desc")
     * @return List of ProfitAnalysis DTOs
     */
    public List<ProfitAnalysis> execute(Double minProfit, Integer maxResults, String sortBy, String order) {
        log.info("Executing GetProfitableSkinsUseCase with minProfit={}, maxResults={}, sortBy={}, order={}",
                minProfit, maxResults, sortBy, order);

        // Step 1: Fetch all skins
        List<Skin> allSkins = skinRepository.findAll();
        log.debug("Found {} total skins", allSkins.size());

        // Step 2: Filter skins with price and build profit analysis
        List<ProfitAnalysis> profitAnalyses = allSkins.stream()
                .filter(skin -> skin.getPrice() != null && skin.getPrice() > 0)
                .map(this::buildProfitAnalysis)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.debug("Built {} profit analyses", profitAnalyses.size());

        // Step 3: Apply minimum profit filter if specified
        if (minProfit != null) {
            profitAnalyses = profitAnalyses.stream()
                    .filter(analysis -> analysis.getProfitPercentage() != null
                            && analysis.getProfitPercentage() >= minProfit)
                    .collect(Collectors.toList());
            log.debug("After minProfit filter: {} analyses", profitAnalyses.size());
        }

        // Step 4: Apply sorting
        String sortField = sortBy != null ? sortBy.toLowerCase() : "profit";
        String sortOrder = order != null ? order.toLowerCase() : "desc";
        Comparator<ProfitAnalysis> comparator = getComparator(sortField);

        if ("asc".equals(sortOrder)) {
            profitAnalyses.sort(comparator);
        } else {
            profitAnalyses.sort(comparator.reversed());
        }

        // Step 5: Apply max results limit if specified
        if (maxResults != null && maxResults > 0 && profitAnalyses.size() > maxResults) {
            profitAnalyses = profitAnalyses.subList(0, maxResults);
            log.debug("Limited to {} results", maxResults);
        }

        log.info("Returning {} profitable skins", profitAnalyses.size());
        return profitAnalyses;
    }

    /**
     * Builds a ProfitAnalysis DTO for a given skin
     *
     * @param skin The skin to analyze
     * @return Optional containing ProfitAnalysis if profit can be calculated, empty otherwise
     */
    private Optional<ProfitAnalysis> buildProfitAnalysis(Skin skin) {
        try {
            // Query Steam price history for this skin + wear combination
            Optional<SteamPriceHistory> historyOpt = priceHistoryRepository
                    .findLatestBySkinNameAndWear(skin.getName(), skin.getWear());

            ProfitAnalysis.ProfitAnalysisBuilder builder = ProfitAnalysis.builder()
                    .skinId(skin.getId())
                    .skinName(skin.getName())
                    .wear(skin.getWear())
                    .marketPrice(skin.getPrice())
                    .marketCurrency(skin.getCurrency())
                    .marketSource(skin.getMarketSource())
                    .link(skin.getLink())
                    .lastUpdated(skin.getUpdatedAt())
                    .hasHistory(historyOpt.isPresent());

            // If Steam price history exists, calculate profit
            if (historyOpt.isPresent()) {
                SteamPriceHistory history = historyOpt.get();
                Long steamPrice = history.getAveragePrice();
                Long lastSalePrice = history.getLastSalePrice();
                Long lowestBuyOrderPrice = history.getLowestBuyOrderPrice();

                try {
                    // Calculate profit with all available price points
                    ProfitResult profitResult = profitCalculationService.calculateProfit(
                            skin.getPrice(),
                            steamPrice,
                            lastSalePrice,
                            lowestBuyOrderPrice
                    );

                    builder.steamAveragePrice(steamPrice)
                            .lastSalePrice(lastSalePrice)
                            .lowestBuyOrderPrice(lowestBuyOrderPrice)
                            .discountPercentage(profitResult.getDiscountPercentage())
                            .profitPercentage(profitResult.getProfitPercentage())
                            .expectedGainUsd(profitResult.getExpectedGainCents())
                            .profitPercentageVsLastSale(profitResult.getProfitPercentageVsLastSale())
                            .profitPercentageVsLowestBuyOrder(profitResult.getProfitPercentageVsLowestBuyOrder());

                    log.debug("Calculated profit for skin {}: profit={}%, profitVsLastSale={}%, profitVsLowestBuyOrder={}%, gain={} cents",
                            skin.getId(),
                            String.format("%.2f", profitResult.getProfitPercentage()),
                            profitResult.getProfitPercentageVsLastSale() != null ? String.format("%.2f", profitResult.getProfitPercentageVsLastSale()) : "N/A",
                            profitResult.getProfitPercentageVsLowestBuyOrder() != null ? String.format("%.2f", profitResult.getProfitPercentageVsLowestBuyOrder()) : "N/A",
                            profitResult.getExpectedGainCents());
                } catch (IllegalArgumentException e) {
                    log.warn("Could not calculate profit for skin {}: {}", skin.getId(), e.getMessage());
                    // Return analysis without profit data but with price information
                    builder.steamAveragePrice(steamPrice)
                            .lastSalePrice(lastSalePrice)
                            .lowestBuyOrderPrice(lowestBuyOrderPrice);
                }
            } else {
                log.debug("No Steam price history found for skin {} ({} - {})",
                        skin.getId(), skin.getName(), skin.getWear());
            }

            return Optional.of(builder.build());
        } catch (Exception e) {
            log.error("Error building profit analysis for skin {}: {}", skin.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Gets the appropriate comparator based on the sort field
     *
     * @param sortField The field to sort by
     * @return Comparator for ProfitAnalysis
     */
    private Comparator<ProfitAnalysis> getComparator(String sortField) {
        switch (sortField) {
            case "discount":
                return Comparator.comparing(
                        analysis -> analysis.getDiscountPercentage() != null ? analysis.getDiscountPercentage() : Double.MIN_VALUE
                );
            case "gain":
                return Comparator.comparing(
                        analysis -> analysis.getExpectedGainUsd() != null ? analysis.getExpectedGainUsd() : Long.MIN_VALUE
                );
            case "profit":
            default:
                return Comparator.comparing(
                        analysis -> analysis.getProfitPercentage() != null ? analysis.getProfitPercentage() : Double.MIN_VALUE
                );
        }
    }
}
