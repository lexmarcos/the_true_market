package com.thetruemarket.api.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thetruemarket.api.domain.exception.ExchangeRateUnavailableException;
import com.thetruemarket.api.domain.service.CurrencyConversionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Currency conversion service implementation using ExchangeRate-API
 * Implements the adapter pattern for external API integration
 * Features cache with TTL validation and fallback strategy
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateApiService implements CurrencyConversionService {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/BRL";
    private static final String CACHE_KEY = "BRL_USD";
    private static final String CACHE_NAME = "exchangeRates";

    private final RestTemplate restTemplate;
    private final CacheManager cacheManager;

    @Value("${exchange-rate.cache.ttl-hours:24}")
    private long cacheTtlHours;

    @Override
    public Long convertBrlToUsd(Long amountInBrl) {
        if (amountInBrl == null || amountInBrl == 0) {
            return 0L;
        }

        try {
            // Get exchange rate with TTL validation
            Double exchangeRate = getExchangeRateWithTtl();

            // Convert: BRL amount * exchange rate = USD amount
            BigDecimal brlAmount = BigDecimal.valueOf(amountInBrl);
            BigDecimal rate = BigDecimal.valueOf(exchangeRate);
            BigDecimal usdAmount = brlAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);

            Long result = usdAmount.longValue();

            log.debug("Converted {} BRL cents to {} USD cents (rate: {})", amountInBrl, result, exchangeRate);

            return result;

        } catch (ExchangeRateUnavailableException e) {
            // Re-throw domain exception
            throw e;
        } catch (Exception e) {
            log.error("Error converting BRL to USD: {}", e.getMessage(), e);
            throw new ExchangeRateUnavailableException("Failed to convert currency from BRL to USD", e);
        }
    }

    /**
     * Gets exchange rate with TTL validation and fallback strategy
     *
     * Strategy:
     * 1. Try to get from cache
     * 2. Validate cache age (< TTL hours)
     * 3. If cache valid: use it
     * 4. If cache expired or missing: call API
     * 5. If API fails AND cache exists (even if old): use cache with warning
     * 6. If API fails AND no cache: throw ExchangeRateUnavailableException
     *
     * @return The exchange rate (BRL to USD)
     * @throws ExchangeRateUnavailableException if rate cannot be obtained
     */
    private Double getExchangeRateWithTtl() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        CachedExchangeRate cachedRate = null;

        // Try to get from cache
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(CACHE_KEY);
            if (wrapper != null && wrapper.get() instanceof CachedExchangeRate) {
                cachedRate = (CachedExchangeRate) wrapper.get();
            }
        }

        // Check if cache is valid (not expired)
        if (cachedRate != null && isCacheValid(cachedRate)) {
            log.debug("Using cached exchange rate (age: {} hours)", getAgeInHours(cachedRate));
            return cachedRate.getRate();
        }

        // Cache expired or missing - try to fetch from API
        try {
            Double rate = fetchExchangeRateFromApi();
            CachedExchangeRate newCachedRate = new CachedExchangeRate(rate, LocalDateTime.now());

            // Store in cache
            if (cache != null) {
                cache.put(CACHE_KEY, newCachedRate);
            }

            return rate;

        } catch (Exception e) {
            // API call failed - use old cache as fallback if available
            if (cachedRate != null) {
                long ageHours = getAgeInHours(cachedRate);
                log.warn("API call failed, using stale cached rate (age: {} hours): {}", ageHours, e.getMessage());
                return cachedRate.getRate();
            }

            // No cache available and API failed
            log.error("Exchange rate unavailable: API failed and no cache available: {}", e.getMessage());
            throw new ExchangeRateUnavailableException(
                "Exchange rate API is unavailable and no cached rate exists", e);
        }
    }

    /**
     * Fetches the current BRL to USD exchange rate from ExchangeRate-API
     *
     * @return The exchange rate (BRL to USD)
     */
    private Double fetchExchangeRateFromApi() {
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
    }

    /**
     * Checks if cached rate is still valid (within TTL)
     */
    private boolean isCacheValid(CachedExchangeRate cachedRate) {
        long ageHours = getAgeInHours(cachedRate);
        return ageHours < cacheTtlHours;
    }

    /**
     * Calculates age of cached rate in hours
     */
    private long getAgeInHours(CachedExchangeRate cachedRate) {
        return ChronoUnit.HOURS.between(cachedRate.getCachedAt(), LocalDateTime.now());
    }

    /**
     * Wrapper class to store exchange rate with timestamp
     */
    @Getter
    @AllArgsConstructor
    private static class CachedExchangeRate {
        private final Double rate;
        private final LocalDateTime cachedAt;
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
