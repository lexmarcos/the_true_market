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
  lastSalePrice: number | null;
  lowestBuyOrderPrice: number | null;
  discountPercentage: number | null;
  profitPercentage: number | null;
  profitPercentageVsLastSale: number | null;
  profitPercentageVsLowestBuyOrder: number | null;
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
  lastSalePrice: string;
  lowestBuyOrderPrice: string;
  discountPercentage: string;
  profitPercentage: string;
  profitPercentageVsLastSale: string;
  profitPercentageVsLowestBuyOrder: string;
  expectedGainUsd: string;
  expectedGainVsLastSale: string;
  expectedGainVsLowestBuyOrder: string;
  expectedGainLabel: string;
  expectedGainColorClass: string;
  expectedGainVsLastSaleColorClass: string;
  expectedGainVsLowestBuyOrderColorClass: string;
  hasHistory: boolean;
  lastUpdated: string;
  lastUpdatedRelative: string;
  profitBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  profitVsLastSaleBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  profitVsLowestBuyOrderBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  discountBadgeVariant: "default" | "secondary" | "destructive" | "outline";
  link: string;
  steamMarketLink: string;
}
