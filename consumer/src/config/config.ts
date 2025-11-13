import dotenv from 'dotenv';
import { Config } from '../types';
import path from 'path';

// Load environment variables from .env file
dotenv.config();

/**
 * Get required environment variable or throw error
 */
function getEnvVar(key: string, defaultValue?: string): string {
  const value = process.env[key] || defaultValue;
  if (value === undefined) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}

/**
 * Get number from environment variable
 */
function getEnvNumber(key: string, defaultValue: number): number {
  const value = process.env[key];
  if (value === undefined) {
    return defaultValue;
  }
  const num = parseInt(value, 10);
  if (isNaN(num)) {
    throw new Error(`Invalid number for environment variable ${key}: ${value}`);
  }
  return num;
}

/**
 * Get boolean from environment variable
 */
function getEnvBoolean(key: string, defaultValue: boolean): boolean {
  const value = process.env[key];
  if (value === undefined) {
    return defaultValue;
  }
  return value.toLowerCase() === 'true' || value === '1';
}

/**
 * Application configuration loaded from environment variables
 */
export const config: Config = {
  api: {
    url: getEnvVar('API_URL', 'https://api.thetruemarket.com'),
    key: getEnvVar('API_KEY', ''),
  },
  browser: {
    dataDir: path.resolve(getEnvVar('BROWSER_DATA_DIR', './browser-data')),
    headless: getEnvBoolean('HEADLESS', false),
  },
  scraping: {
    pollIntervalMs: getEnvNumber('POLL_INTERVAL_MS', 60000),
    rateLimitDelayMs: getEnvNumber('RATE_LIMIT_DELAY_MS', 3000),
    priceHistoryAverageCount: getEnvNumber('PRICE_HISTORY_AVERAGE_COUNT', 10),
  },
  logging: {
    level: getEnvVar('LOG_LEVEL', 'info'),
    dir: path.resolve(getEnvVar('LOG_DIR', './logs')),
  },
  steam: {
    loginTimeoutMs: getEnvNumber('STEAM_LOGIN_TIMEOUT_MS', 300000),
  },
};

/**
 * Validate configuration
 */
export function validateConfig(): void {
  // Add custom validation logic here
  if (config.scraping.pollIntervalMs < 1000) {
    throw new Error('POLL_INTERVAL_MS must be at least 1000ms');
  }
  if (config.scraping.rateLimitDelayMs < 500) {
    throw new Error('RATE_LIMIT_DELAY_MS must be at least 500ms');
  }
}

// Validate on load
validateConfig();
