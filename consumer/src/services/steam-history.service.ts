import { Page } from 'puppeteer';
import { createLogger } from '../utils/logger';
import { SteamPriceHistory } from '../types';
import { config } from '../config/config';

const logger = createLogger('SteamHistoryService');

export class SteamHistoryService {
  private readonly STEAM_PRICE_HISTORY_BASE = 'https://steamcommunity.com/market/pricehistory/';

  /**
   * Convert wear format from API to Steam format
   * FIELD_TESTED -> Field-Tested
   */
  private convertWearToSteamFormat(wear: string): string {
    const wearMap: Record<string, string> = {
      'FIELD_TESTED': 'Field-Tested',
      'MINIMAL_WEAR': 'Minimal Wear',
      'FACTORY_NEW': 'Factory New',
      'WELL_WORN': 'Well-Worn',
      'BATTLE_SCARRED': 'Battle-Scarred',
    };

    return wearMap[wear] || wear;
  }

  /**
   * Remove any existing wear from skin name
   * "AK-47 | Midnight Laminate (Factory New)" -> "AK-47 | Midnight Laminate"
   */
  private cleanSkinName(skinName: string): string {
    // Remove anything between parentheses at the end of the string
    return skinName.replace(/\s*\([^)]*\)\s*$/, '').trim();
  }

  /**
   * Get price history for a skin from Steam
   */
  async getPriceHistory(page: Page, skinName: string, wear: string): Promise<SteamPriceHistory | null> {
    try {
      // Clean skin name (remove any existing wear)
      const cleanedSkinName = this.cleanSkinName(skinName);

      // Convert wear to Steam format
      const steamWear = this.convertWearToSteamFormat(wear);

      // Build market hash name: "AK-47 | Redline (Field-Tested)"
      const marketHashName = `${cleanedSkinName} (${steamWear})`;

      // Build URL
      const url = `${this.STEAM_PRICE_HISTORY_BASE}?appid=730&market_hash_name=${encodeURIComponent(marketHashName)}`;

      logger.info({
        originalSkinName: skinName,
        cleanedSkinName,
        wear,
        steamWear,
        marketHashName,
        url
      }, 'Fetching price history from Steam');

      // Navigate to the page
      await page.goto(url, { waitUntil: 'networkidle2', timeout: 30000 });

      // Apply rate limiting
      await this.delay(config.scraping.rateLimitDelayMs);

      // Extract JSON from page body
      const jsonData = await page.evaluate(() => {
        return document.body.innerText;
      });

      // Parse JSON
      const priceHistory: SteamPriceHistory = JSON.parse(jsonData);

      if (!priceHistory.success) {
        logger.warn({ skinName, wear }, 'Steam returned success=false');
        return null;
      }

      logger.info({
        skinName,
        wear,
        priceCount: priceHistory.prices?.length || 0,
      }, 'Price history fetched successfully');

      return priceHistory;
    } catch (error) {
      logger.error({ error, skinName, wear }, 'Error fetching price history');
      return null;
    }
  }

  /**
   * Calculate average price from last 10 entries
   */
  calculateAveragePrice(priceHistory: SteamPriceHistory): number | null {
    try {
      if (!priceHistory.prices || priceHistory.prices.length === 0) {
        logger.warn('Price history is empty');
        return null;
      }

      // Get last N prices (or less if not enough data) from config
      const count = config.scraping.priceHistoryAverageCount;
      const lastN = priceHistory.prices.slice(-count);

      logger.info({ totalPrices: priceHistory.prices.length, usedForAverage: lastN.length, configuredCount: count }, 'Calculating average');

      // Extract prices (index [1] from each array)
      const prices = lastN.map(entry => entry[1]);

      // Calculate average
      const sum = prices.reduce((acc, price) => acc + price, 0);
      const average = sum / prices.length;

      // Convert to cents (multiply by 100) and round to integer
      const averageInCents = Math.round(average * 100);

      logger.info({ prices, average, averageInCents }, 'Average price calculated');

      return averageInCents;
    } catch (error) {
      logger.error({ error }, 'Error calculating average price');
      return null;
    }
  }

  /**
   * Get the last sale price (most recent price in history)
   */
  getLastSalePrice(priceHistory: SteamPriceHistory): number | null {
    try {
      if (!priceHistory.prices || priceHistory.prices.length === 0) {
        logger.warn('Price history is empty, cannot get last sale price');
        return null;
      }

      // Get the last price entry (most recent)
      const lastEntry = priceHistory.prices[priceHistory.prices.length - 1];
      const lastPrice = lastEntry[1]; // Index [1] contains the price

      // Convert to cents (multiply by 100) and round to integer
      const lastPriceInCents = Math.round(lastPrice * 100);

      logger.info({
        lastPrice,
        lastPriceInCents,
        date: lastEntry[0]
      }, 'Last sale price extracted');

      return lastPriceInCents;
    } catch (error) {
      logger.error({ error }, 'Error getting last sale price');
      return null;
    }
  }

  /**
   * Delay helper
   */
  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}

// Export singleton instance
export const steamHistoryService = new SteamHistoryService();
