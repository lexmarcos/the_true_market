/**
 * Exchange Rate Utilities
 * Fetches and caches currency exchange rates from fawazahmed0 API
 */

const CACHE_KEY = "exchange_rates";
const CACHE_TIMESTAMP_KEY = "exchange_rates_timestamp";
const CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

export interface ExchangeRates {
  usd: number;
  brl: number;
  eur: number;
}

interface CurrencyAPIResponse {
  date: string;
  usd: {
    brl: number;
    eur: number;
    [key: string]: number;
  };
}

/**
 * Fetch exchange rates from fawazahmed0 API
 * Returns cached rates if available and not expired, or fetches fresh rates
 */
export async function fetchExchangeRates(): Promise<ExchangeRates> {
  // Try to get cached rates first
  const cachedRates = getCachedRates();
  if (cachedRates) {
    return cachedRates;
  }

  try {
    // Fetch fresh rates from API
    const response = await fetch(
      "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"
    );

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    const data: CurrencyAPIResponse = await response.json();

    const rates: ExchangeRates = {
      usd: 1,
      brl: data.usd.brl || 1,
      eur: data.usd.eur || 1,
    };

    // Cache the rates
    cacheRates(rates);

    return rates;
  } catch (error) {
    console.error("Failed to fetch exchange rates:", error);

    // Try to return stale cached rates as fallback
    const staleRates = getStaleRates();
    if (staleRates) {
      console.warn("Using stale cached rates as fallback");
      return staleRates;
    }

    // Ultimate fallback: default 1:1 rates
    return {
      usd: 1,
      brl: 1,
      eur: 1,
    };
  }
}

/**
 * Get cached rates if they exist and are not expired
 */
function getCachedRates(): ExchangeRates | null {
  if (typeof window === "undefined") return null;

  try {
    const cachedRates = localStorage.getItem(CACHE_KEY);
    const timestamp = localStorage.getItem(CACHE_TIMESTAMP_KEY);

    if (!cachedRates || !timestamp) {
      return null;
    }

    const age = Date.now() - parseInt(timestamp, 10);
    if (age > CACHE_DURATION) {
      return null;
    }

    return JSON.parse(cachedRates) as ExchangeRates;
  } catch (error) {
    console.error("Error reading cached rates:", error);
    return null;
  }
}

/**
 * Get stale cached rates (expired but available) as fallback
 */
function getStaleRates(): ExchangeRates | null {
  if (typeof window === "undefined") return null;

  try {
    const cachedRates = localStorage.getItem(CACHE_KEY);
    if (!cachedRates) {
      return null;
    }

    return JSON.parse(cachedRates) as ExchangeRates;
  } catch (error) {
    console.error("Error reading stale rates:", error);
    return null;
  }
}

/**
 * Cache exchange rates in localStorage with timestamp
 */
function cacheRates(rates: ExchangeRates): void {
  if (typeof window === "undefined") return;

  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify(rates));
    localStorage.setItem(CACHE_TIMESTAMP_KEY, Date.now().toString());
  } catch (error) {
    console.error("Error caching rates:", error);
  }
}
