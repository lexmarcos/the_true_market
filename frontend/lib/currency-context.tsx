"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { fetchExchangeRates, ExchangeRates } from "./exchange-rates";

export type Currency = "USD" | "BRL" | "EUR";

interface CurrencyContextType {
  selectedCurrency: Currency;
  exchangeRates: ExchangeRates;
  setSelectedCurrency: (currency: Currency) => void;
  convertFromUSD: (usdCents: number, toCurrency: Currency) => number;
  isLoading: boolean;
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(
  undefined
);

const CURRENCY_PREFERENCE_KEY = "preferred_currency";

/**
 * CurrencyProvider component
 * Manages global currency state, exchange rates, and conversion logic
 */
export function CurrencyProvider({ children }: { children: React.ReactNode }) {
  const [selectedCurrency, setSelectedCurrencyState] = useState<Currency>("USD");
  const [exchangeRates, setExchangeRates] = useState<ExchangeRates>({
    usd: 1,
    brl: 1,
    eur: 1,
  });
  const [isLoading, setIsLoading] = useState(false);

  // Load saved currency preference from localStorage on mount
  useEffect(() => {
    if (typeof window === "undefined") return;

    try {
      const savedCurrency = localStorage.getItem(CURRENCY_PREFERENCE_KEY);
      if (savedCurrency && ["USD", "BRL", "EUR"].includes(savedCurrency)) {
        setSelectedCurrencyState(savedCurrency as Currency);
      }
    } catch (error) {
      console.error("Error loading currency preference:", error);
    }
  }, []);

  // Fetch exchange rates on mount
  useEffect(() => {
    async function loadRates() {
      setIsLoading(true);
      try {
        const rates = await fetchExchangeRates();
        setExchangeRates(rates);
      } catch (error) {
        console.error("Error loading exchange rates:", error);
      } finally {
        setIsLoading(false);
      }
    }

    loadRates();
  }, []);

  // Persist currency preference to localStorage
  const setSelectedCurrency = (currency: Currency) => {
    setSelectedCurrencyState(currency);
    
    if (typeof window !== "undefined") {
      try {
        localStorage.setItem(CURRENCY_PREFERENCE_KEY, currency);
      } catch (error) {
        console.error("Error saving currency preference:", error);
      }
    }
  };

  /**
   * Convert USD cents to target currency cents
   * @param usdCents Amount in USD cents
   * @param toCurrency Target currency
   * @returns Amount in target currency cents
   */
  const convertFromUSD = (usdCents: number, toCurrency: Currency): number => {
    const currencyKey = toCurrency.toLowerCase() as keyof ExchangeRates;
    const rate = exchangeRates[currencyKey];
    return Math.round(usdCents * rate);
  };

  const value: CurrencyContextType = {
    selectedCurrency,
    exchangeRates,
    setSelectedCurrency,
    convertFromUSD,
    isLoading,
  };

  return (
    <CurrencyContext.Provider value={value}>
      {children}
    </CurrencyContext.Provider>
  );
}

/**
 * useCurrency hook
 * Access currency context from any component
 */
export function useCurrency() {
  const context = useContext(CurrencyContext);
  if (context === undefined) {
    throw new Error("useCurrency must be used within a CurrencyProvider");
  }
  return context;
}
