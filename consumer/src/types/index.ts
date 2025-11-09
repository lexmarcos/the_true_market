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
