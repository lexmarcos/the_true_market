package com.thetruemarket.api.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Currency conversion service implementation using ExchangeRate-API
 * Implements the adapter pattern for external API integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateApiService implements CurrencyConversionService {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/BRL";
    private final RestTemplate restTemplate;

    @Override
    @Cacheable(value = "exchangeRates", unless = "#result == null")
    public Long convertBrlToUsd(Long amountInBrl) {
        if (amountInBrl == null || amountInBrl == 0) {
            return 0L;
        }

        try {
            // Get exchange rate from API
            Double exchangeRate = getExchangeRate();

            // Convert: BRL amount * exchange rate = USD amount
            BigDecimal brlAmount = BigDecimal.valueOf(amountInBrl);
            BigDecimal rate = BigDecimal.valueOf(exchangeRate);
            BigDecimal usdAmount = brlAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);

            Long result = usdAmount.longValue();

            log.debug("Converted {} BRL cents to {} USD cents (rate: {})", amountInBrl, result, exchangeRate);

            return result;

        } catch (Exception e) {
            log.error("Error converting BRL to USD: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert currency from BRL to USD", e);
        }
    }

    /**
     * Fetches the current BRL to USD exchange rate from ExchangeRate-API
     * Result is cached to avoid excessive API calls
     *
     * @return The exchange rate (BRL to USD)
     */
    @Cacheable(value = "exchangeRates", key = "'BRL_USD'")
    private Double getExchangeRate() {
        try {
            log.info("Fetching BRL to USD exchange rate from ExchangeRate-API");

            ExchangeRateResponse response = restTemplate.getForObject(API_URL, ExchangeRateResponse.class);

            if (response == null || response.getRates() == null) {
                throw new RuntimeException("Invalid response from ExchangeRate-API");
            }

            Double usdRate = response.getRates().get("USD");
            if (usdRate == null) {
                throw new RuntimeException("USD rate not found in ExchangeRate-API response");
            }

            log.info("Successfully fetched exchange rate: 1 BRL = {} USD", usdRate);

            return usdRate;

        } catch (Exception e) {
            log.error("Error fetching exchange rate from API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch exchange rate from ExchangeRate-API", e);
        }
    }

    /**
     * Response DTO for ExchangeRate-API
     */
    @Data
    private static class ExchangeRateResponse {
        @JsonProperty("base")
        private String base;

        @JsonProperty("date")
        private String date;

        @JsonProperty("rates")
        private Map<String, Double> rates;
    }
}
