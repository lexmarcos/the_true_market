package com.thetruemarket.api.infrastructure.web.controller;

import com.thetruemarket.api.application.dto.ProfitAnalysis;
import com.thetruemarket.api.application.usecase.GetProfitableSkinsUseCase;
import com.thetruemarket.api.infrastructure.web.dto.ProfitableSkinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for skin profit analysis
 * Exposes endpoints for retrieving profitable skins with profit calculations
 */
@RestController
@RequestMapping("/api/v1/skins")
@RequiredArgsConstructor
@Slf4j
public class SkinProfitController {
    private final GetProfitableSkinsUseCase getProfitableSkinsUseCase;

    /**
     * GET /api/v1/skins/profitable
     * Retrieves all skins with profit analysis based on Steam price history
     *
     * @param minProfit Minimum profit percentage to filter (optional)
     * @param maxResults Maximum number of results to return (optional)
     * @param sortBy Field to sort by: "profit", "discount", "gain" (optional, defaults to "profit")
     * @param order Sort order: "asc" or "desc" (optional, defaults to "desc")
     * @return List of profitable skins with profit analysis
     */
    @GetMapping("/profitable")
    public ResponseEntity<List<ProfitableSkinResponse>> getProfitableSkins(
            @RequestParam(required = false) Double minProfit,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false, defaultValue = "profit") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order
    ) {
        log.info("GET /api/v1/skins/profitable - minProfit={}, maxResults={}, sortBy={}, order={}",
                minProfit, maxResults, sortBy, order);

        try {
            List<ProfitAnalysis> profitAnalyses = getProfitableSkinsUseCase.execute(
                    minProfit,
                    maxResults,
                    sortBy,
                    order
            );

            List<ProfitableSkinResponse> response = profitAnalyses.stream()
                    .map(ProfitableSkinResponse::fromApplication)
                    .collect(Collectors.toList());

            log.info("Returning {} profitable skins", response.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving profitable skins: {}", e.getMessage(), e);
            throw e;
        }
    }
}
