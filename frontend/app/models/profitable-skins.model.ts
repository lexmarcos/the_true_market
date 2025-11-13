"use client";

import { useMemo } from "react";
import useSWR from "swr";
import { api } from "@/lib/api";
import {
  formatCurrency,
  formatPercentage,
  formatDate,
  formatRelativeTime,
  formatWear,
  formatMarketSource,
} from "@/lib/format";
import {
  ProfitableSkin,
  ProfitableSkinsParams,
  SkinCardData,
} from "../types/profitable-skins.types";
import { profitableSkinsResponseSchema } from "../types/profitable-skins.schemas";

interface UseProfitableSkinsResult {
  skins: SkinCardData[];
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  totalCount: number;
  hasProfitableSkins: boolean;
}

/**
 * Fetcher function for SWR
 * Fetches and validates profitable skins from the API
 */
async function fetchProfitableSkins(
  url: string,
  params?: ProfitableSkinsParams
): Promise<ProfitableSkin[]> {
  const response = await api.get<ProfitableSkin[]>(url, {
    params: params as Record<string, string | number | boolean | undefined>,
  });

  // Validate response with Zod
  return profitableSkinsResponseSchema.parse(response);
}

/**
 * Model hook for profitable skins
 * Contains ALL business logic, state management, and data transformations
 * Uses SWR for efficient data fetching and caching
 */
export function useProfitableSkins(
  params?: ProfitableSkinsParams
): UseProfitableSkinsResult {
  // Generate cache key based on params
  const cacheKey = useMemo(() => {
    const queryParams = new URLSearchParams();
    if (params?.minProfit !== undefined) {
      queryParams.append("minProfit", String(params.minProfit));
    }
    if (params?.maxResults !== undefined) {
      queryParams.append("maxResults", String(params.maxResults));
    }
    if (params?.sortBy) {
      queryParams.append("sortBy", params.sortBy);
    }
    if (params?.order) {
      queryParams.append("order", params.order);
    }
    const queryString = queryParams.toString();
    return queryString
      ? `/api/v1/skins/profitable?${queryString}`
      : "/api/v1/skins/profitable";
  }, [params?.minProfit, params?.maxResults, params?.sortBy, params?.order]);

  // Fetch data with SWR
  const { data: rawSkins, error: swrError, isLoading, mutate } = useSWR(
    cacheKey,
    () => fetchProfitableSkins("/api/v1/skins/profitable", params),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: true,
      dedupingInterval: 5000, // Prevent duplicate requests within 5s
    }
  );

  // Convert SWR error to string
  const error = swrError
    ? swrError instanceof Error
      ? swrError.message
      : "Failed to fetch skins"
    : null;

  // Transform raw skins into formatted data for the view
  const transformedSkins: SkinCardData[] = useMemo(() => {
    if (!rawSkins) return [];

    return rawSkins.map((skin) => {
      // Determine profit badge variant based on profit percentage
      const profitBadgeVariant = determineProfitBadgeVariant(
        skin.profitPercentage
      );

      // Determine discount badge variant based on discount percentage
      const discountBadgeVariant = determineDiscountBadgeVariant(
        skin.discountPercentage
      );

      // Determine if it's a gain or loss
      const isGain = (skin.expectedGainUsd ?? 0) >= 0;
      const expectedGainLabel = isGain ? "Ganho esperado" : "Perda esperada";
      const expectedGainColorClass = isGain
        ? "text-green-600 dark:text-green-400"
        : "text-red-600 dark:text-red-400";

      // Generate Steam Market link
      const steamMarketLink = generateSteamMarketLink(skin.skinName);

      return {
        skinId: skin.skinId,
        skinName: skin.skinName,
        wear: skin.wear,
        wearDisplay: formatWear(skin.wear),
        marketPrice: formatCurrency(skin.marketPrice, skin.marketCurrency),
        marketSource: skin.marketSource,
        marketSourceDisplay: formatMarketSource(skin.marketSource),
        steamAveragePrice: formatCurrency(skin.steamAveragePrice, "USD"),
        discountPercentage: formatPercentage(skin.discountPercentage),
        profitPercentage: formatPercentage(skin.profitPercentage),
        expectedGainUsd: formatCurrency(skin.expectedGainUsd, "USD"),
        expectedGainLabel,
        expectedGainColorClass,
        hasHistory: skin.hasHistory,
        lastUpdated: formatDate(skin.lastUpdated),
        lastUpdatedRelative: formatRelativeTime(skin.lastUpdated),
        profitBadgeVariant,
        discountBadgeVariant,
        link: skin.link,
        steamMarketLink,
      };
    });
  }, [rawSkins]);

  // Calculate total count
  const totalCount = transformedSkins.length;

  // Check if there are any profitable skins
  const hasProfitableSkins = totalCount > 0;

  // Refetch function that triggers SWR revalidation
  const refetch = async () => {
    await mutate();
  };

  return {
    skins: transformedSkins,
    isLoading,
    error,
    refetch,
    totalCount,
    hasProfitableSkins,
  };
}

/**
 * Determine badge variant based on profit percentage
 * Business logic for visual representation
 * @param profitPercentage - Percentage in cents (e.g., 2000 = 20%)
 */
function determineProfitBadgeVariant(
  profitPercentage: number | null
): "default" | "secondary" | "destructive" | "outline" {
  if (profitPercentage === null) {
    return "outline";
  }

  if (profitPercentage >= 2000) {
    return "default"; // Green/Success - High profit (>= 20%)
  }

  if (profitPercentage >= 1000) {
    return "secondary"; // Blue - Medium profit (>= 10%)
  }

  if (profitPercentage >= 500) {
    return "outline"; // Gray - Low profit (>= 5%)
  }

  return "destructive"; // Red - Very low/negative profit (< 5%)
}

/**
 * Determine badge variant based on discount percentage
 * Business logic for visual representation
 * @param discountPercentage - Percentage in cents (e.g., 3000 = 30%)
 */
function determineDiscountBadgeVariant(
  discountPercentage: number | null
): "default" | "secondary" | "destructive" | "outline" {
  if (discountPercentage === null) {
    return "outline";
  }

  if (discountPercentage >= 3000) {
    return "default"; // Green - High discount (>= 30%)
  }

  if (discountPercentage >= 1500) {
    return "secondary"; // Blue - Medium discount (>= 15%)
  }

  if (discountPercentage >= 500) {
    return "outline"; // Gray - Low discount (>= 5%)
  }

  return "destructive"; // Red - Very low/no discount (< 5%)
}

/**
 * Generate Steam Community Market link for a skin
 * @param skinName - Full skin name (e.g., "AK-47 | Redline (Field-Tested)")
 * @returns Steam Market URL
 */
function generateSteamMarketLink(skinName: string): string {
  const baseUrl = "https://steamcommunity.com/market/listings/730";
  const encodedName = encodeURIComponent(skinName);
  return `${baseUrl}/${encodedName}`;
}
