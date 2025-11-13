/**
 * Represents a profitable skin from the API
 */
export interface ProfitableSkin {
  skinId: string;
  skinName: string;
  wear: string;
  marketPrice: number;
  marketCurrency: string;
  marketSource: string;
  steamAveragePrice: number | null;
  discountPercentage: number | null;
  profitPercentage: number | null;
  expectedGainUsd: number | null;
  hasHistory: boolean;
  lastUpdated: string | null;
  link: string;
}

/**
 * Query parameters for fetching profitable skins
 */
export interface ProfitableSkinsParams {
  minProfit?: number;
  maxResults?: number;
  sortBy?: "profit" | "discount" | "gain";
  order?: "asc" | "desc";
}

/**
 * API response type
 */
export type ProfitableSkinsResponse = ProfitableSkin[];

/**
 * Skin card data for the view layer
 */
export interface SkinCardData {
  skinId: string;
  skinName: string;
  wear: string;
  wearDisplay: string;
  marketPrice: string;
  marketSource: string;
  marketSourceDisplay: string;
  steamAveragePrice: string;
  discountPercentage: string;
  profitPercentage: string;
  expectedGainUsd: string;
  expectedGainLabel: string;
  expectedGainColorClass: string;
  hasHistory: boolean;
  lastUpdated: string;
  lastUpdatedRelative: string;
  profitBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  discountBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  link: string;
  steamMarketLink: string;
}
