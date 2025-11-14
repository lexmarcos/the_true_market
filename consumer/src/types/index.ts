/**
 * Configuration interface for the application
 */
export interface Config {
  api: {
    url: string;
    key: string;
  };
  browser: {
    dataDir: string;
    headless: boolean;
  };
  scraping: {
    pollIntervalMs: number;
    rateLimitDelayMs: number;
    priceHistoryAverageCount: number;
  };
  logging: {
    level: string;
    dir: string;
  };
  steam: {
    loginTimeoutMs: number;
  };
}

/**
 * Steam item data collected from scraping
 */
export interface ItemData {
  name: string;
  price: number;
  currency: string;
  imageUrl?: string;
  marketHashName: string;
  appId: number;
  timestamp: Date;
  source: 'steam' | 'bitskins';
  url: string;
}

/**
 * Steam session information
 */
export interface SteamSession {
  isLoggedIn: boolean;
  sessionId?: string;
  steamId?: string;
  cookies?: Array<{
    name: string;
    value: string;
    domain: string;
  }>;
}

/**
 * API response from TheTrueMarket
 */
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

/**
 * Scraping result with status
 */
export interface ScrapeResult {
  success: boolean;
  item?: ItemData;
  error?: string;
}

/**
 * Browser service options
 */
export interface BrowserOptions {
  headless: boolean;
  userDataDir: string;
  viewport?: {
    width: number;
    height: number;
  };
}

/**
 * Logger levels
 */
export type LogLevel = 'trace' | 'debug' | 'info' | 'warn' | 'error' | 'fatal';

/**
 * History Update Task from API
 */
export interface HistoryUpdateTask {
  id: number;
  skinName: string;
  wear: string;
  status: 'WAITING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  finishedAt: string | null;
}

/**
 * Steam price history response
 */
export interface SteamPriceHistory {
  success: boolean;
  price_prefix: string;
  price_suffix: string;
  prices: [string, number, string][]; // [date, price, volume]
}

/**
 * Complete task payload
 */
export interface CompleteTaskPayload {
  skinName: string;
  wear: string;
  averagePrice: number;
  lastSalePrice: number;
  lowestBuyOrderPrice: number;
}
