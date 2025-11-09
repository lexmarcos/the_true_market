import { Page } from 'puppeteer';
import { createLogger } from '../utils/logger';
import { ItemData, ScrapeResult } from '../types';
import { config } from '../config/config';

const logger = createLogger('SteamScraperService');

export class SteamScraperService {
  private readonly STEAM_MARKET_BASE = 'https://steamcommunity.com/market/listings/';

  /**
   * Scrape item data from Steam Market
   */
  async scrapeItem(page: Page, appId: number, marketHashName: string): Promise<ScrapeResult> {
    try {
      const url = `${this.STEAM_MARKET_BASE}${appId}/${encodeURIComponent(marketHashName)}`;
      logger.info({ url }, `Scraping item: ${marketHashName}`);

      // Navigate to item page
      await page.goto(url, { waitUntil: 'networkidle2' });

      // Apply rate limiting
      await this.delay(config.scraping.rateLimitDelayMs);

      // Extract item data
      const itemData = await page.evaluate((url, appId, marketHashName) => {
        // Get item name
        const nameElement = document.querySelector('.market_listing_item_name');
        const name = nameElement?.textContent?.trim() || marketHashName;

        // Get lowest price
        const priceElement = document.querySelector('.market_listing_price.market_listing_price_with_fee');
        const priceText = priceElement?.textContent?.trim() || '';

        // Parse price (remove currency symbols and convert to number)
        let price = 0;
        let currency = 'USD';

        const priceMatch = priceText.match(/([A-Z$€£]+)\s*([\d.,]+)/);
        if (priceMatch) {
          currency = priceMatch[1].replace('$', 'USD').replace('€', 'EUR').replace('£', 'GBP');
          price = parseFloat(priceMatch[2].replace(',', '.'));
        }

        // Get image URL
        const imageElement = document.querySelector('.market_listing_item_img') as HTMLImageElement;
        const imageUrl = imageElement?.src || '';

        return {
          name,
          price,
          currency,
          imageUrl,
          marketHashName,
          appId,
          timestamp: new Date().toISOString(),
          source: 'steam',
          url,
        };
      }, url, appId, marketHashName);

      // Validate data
      if (!itemData.name || itemData.price === 0) {
        logger.warn({ itemData }, 'Failed to extract complete item data');
        return {
          success: false,
          error: 'Incomplete item data',
        };
      }

      logger.info({
        name: itemData.name,
        price: itemData.price,
        currency: itemData.currency,
      }, 'Item scraped successfully');

      return {
        success: true,
        item: {
          ...itemData,
          timestamp: new Date(itemData.timestamp),
          source: itemData.source as 'steam',
        },
      };
    } catch (error) {
      logger.error({ error, appId, marketHashName }, 'Error scraping item');
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Scrape multiple items
   */
  async scrapeMultipleItems(
    page: Page,
    items: Array<{ appId: number; marketHashName: string }>
  ): Promise<ItemData[]> {
    const results: ItemData[] = [];

    logger.info(`Scraping ${items.length} items...`);

    for (const item of items) {
      const result = await this.scrapeItem(page, item.appId, item.marketHashName);

      if (result.success && result.item) {
        results.push(result.item);
      }

      // Apply delay between items to avoid rate limiting
      await this.delay(config.scraping.rateLimitDelayMs);
    }

    logger.info(`Scraped ${results.length}/${items.length} items successfully`);
    return results;
  }

  /**
   * Search for items on Steam Market
   */
  async searchItems(page: Page, query: string): Promise<Array<{ appId: number; marketHashName: string; name: string }>> {
    try {
      const searchUrl = `https://steamcommunity.com/market/search?q=${encodeURIComponent(query)}`;
      logger.info(`Searching for items: ${query}`);

      await page.goto(searchUrl, { waitUntil: 'networkidle2' });

      // Wait for search results
      await page.waitForSelector('.market_listing_row', { timeout: 10000 });

      // Extract search results
      const items = await page.evaluate(() => {
        const results: Array<{ appId: number; marketHashName: string; name: string }> = [];
        const rows = document.querySelectorAll('.market_listing_row');

        rows.forEach((row) => {
          const link = row.querySelector('a.market_listing_item_name_link') as HTMLAnchorElement;
          if (!link) return;

          const href = link.href;
          const name = link.textContent?.trim() || '';

          // Extract appId and market hash name from URL
          // Format: /market/listings/{appId}/{marketHashName}
          const match = href.match(/\/market\/listings\/(\d+)\/([^?]+)/);
          if (match) {
            results.push({
              appId: parseInt(match[1], 10),
              marketHashName: decodeURIComponent(match[2]),
              name,
            });
          }
        });

        return results;
      });

      logger.info(`Found ${items.length} items matching "${query}"`);
      return items;
    } catch (error) {
      logger.error({ error, query }, 'Error searching items');
      return [];
    }
  }

  /**
   * Get popular items from Steam Market
   */
  async getPopularItems(page: Page, appId: number = 730): Promise<Array<{ appId: number; marketHashName: string; name: string }>> {
    try {
      const url = `https://steamcommunity.com/market/search?appid=${appId}`;
      logger.info(`Getting popular items for app ${appId}`);

      await page.goto(url, { waitUntil: 'networkidle2' });
      await page.waitForSelector('.market_listing_row', { timeout: 10000 });

      const items = await page.evaluate((currentAppId) => {
        const results: Array<{ appId: number; marketHashName: string; name: string }> = [];
        const rows = document.querySelectorAll('.market_listing_row');

        rows.forEach((row) => {
          const link = row.querySelector('a.market_listing_item_name_link') as HTMLAnchorElement;
          if (!link) return;

          const href = link.href;
          const name = link.textContent?.trim() || '';

          const match = href.match(/\/market\/listings\/(\d+)\/([^?]+)/);
          if (match) {
            results.push({
              appId: parseInt(match[1], 10),
              marketHashName: decodeURIComponent(match[2]),
              name,
            });
          }
        });

        return results;
      }, appId);

      logger.info(`Found ${items.length} popular items`);
      return items;
    } catch (error) {
      logger.error({ error, appId }, 'Error getting popular items');
      return [];
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
export const steamScraperService = new SteamScraperService();
