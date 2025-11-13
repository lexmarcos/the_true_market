import { z } from "zod";

/**
 * Schema for a single profitable skin
 */
export const profitableSkinSchema = z.object({
  skinId: z.string(),
  skinName: z.string(),
  wear: z.string(),
  marketPrice: z.number(),
  marketCurrency: z.string(),
  marketSource: z.string(),
  steamAveragePrice: z.number().nullable(),
  discountPercentage: z.number().nullable(),
  profitPercentage: z.number().nullable(),
  expectedGainUsd: z.number().nullable(),
  hasHistory: z.boolean(),
  lastUpdated: z.string().nullable(),
  link: z.string(),
});

/**
 * Schema for the API response (array of profitable skins)
 */
export const profitableSkinsResponseSchema = z.array(profitableSkinSchema);

/**
 * Schema for query parameters
 */
export const profitableSkinsParamsSchema = z.object({
  minProfit: z.number().optional(),
  maxResults: z.number().positive().optional(),
  sortBy: z.enum(["profit", "discount", "gain"]).optional(),
  order: z.enum(["asc", "desc"]).optional(),
});
