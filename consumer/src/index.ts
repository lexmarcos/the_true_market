import { Page } from 'puppeteer';
import { browserService } from './services/browser.service';
import { steamAuthService } from './services/steam-auth.service';
import { steamScraperService } from './services/steam-scraper.service';
import { apiService } from './services/api.service';
import { config } from './config/config';
import logger, { createLogger } from './utils/logger';

const appLogger = createLogger('App');

/**
 * Main orchestrator class for Steam price collection
 */
class SteamPriceCollector {
  private isRunning = false;
  private page: Page | null = null;

  /**
   * Initialize the collector
   */
  async initialize(): Promise<void> {
    try {
      appLogger.info('=== Steam Price Collector Starting ===');
      appLogger.info({
        pollInterval: config.scraping.pollIntervalMs,
        rateLimitDelay: config.scraping.rateLimitDelayMs,
        headless: config.browser.headless,
      }, 'Configuration');

      // 1. Initialize browser
      await browserService.initBrowser();
      appLogger.info('Browser initialized successfully');

      // 2. Create page
      this.page = await browserService.createPage();
      appLogger.info('Page created successfully');

      // 3. Ensure user is logged in
      const isLoggedIn = await steamAuthService.ensureLoggedIn(this.page);
      if (!isLoggedIn) {
        throw new Error('Failed to authenticate with Steam');
      }

      // 4. Check API health
      const apiHealthy = await apiService.healthCheck();
      if (!apiHealthy) {
        appLogger.warn('API health check failed - continuing anyway');
      }

      appLogger.info('=== Initialization Complete ===');
    } catch (error) {
      appLogger.error({ error }, 'Initialization failed');
      throw error;
    }
  }

  /**
   * Start the collection loop
   */
  async start(): Promise<void> {
    if (!this.page) {
      throw new Error('Collector not initialized. Call initialize() first.');
    }

    this.isRunning = true;
    appLogger.info('Starting price collection loop...');

    while (this.isRunning) {
      try {
        await this.collectAndSendPrices();

        // Wait for next poll interval
        appLogger.info(`Waiting ${config.scraping.pollIntervalMs}ms until next collection...`);
        await this.delay(config.scraping.pollIntervalMs);
      } catch (error) {
        appLogger.error({ error }, 'Error in collection loop');
        appLogger.info('Retrying in 30 seconds...');
        await this.delay(30000);
      }
    }
  }

  /**
   * Collect prices and send to API
   */
  private async collectAndSendPrices(): Promise<void> {
    if (!this.page) return;

    try {
      appLogger.info('=== Starting Collection Cycle ===');

      // Example: Get popular CS:GO items
      // You can customize this to search specific items or get from a queue
      const items = await steamScraperService.getPopularItems(this.page, 730);
      appLogger.info(`Found ${items.length} items to scrape`);

      if (items.length === 0) {
        appLogger.warn('No items found to scrape');
        return;
      }

      // Take first 10 items as example
      const itemsToScrape = items.slice(0, 10);
      appLogger.info(`Scraping ${itemsToScrape.length} items...`);

      // Scrape items
      const scrapedData = await steamScraperService.scrapeMultipleItems(this.page, itemsToScrape);
      appLogger.info(`Successfully scraped ${scrapedData.length} items`);

      if (scrapedData.length > 0) {
        // Send to API
        if (config.api.key) {
          const result = await apiService.sendBulkItemData(scrapedData);
          appLogger.info({
            success: result.success,
            itemCount: scrapedData.length,
          }, 'Data sent to API');
        } else {
          appLogger.warn('No API key configured - skipping API submission');
          appLogger.info({ scrapedData }, 'Scraped data');
        }
      }

      appLogger.info('=== Collection Cycle Complete ===');
    } catch (error) {
      appLogger.error({ error }, 'Error collecting prices');
      throw error;
    }
  }

  /**
   * Stop the collector
   */
  async stop(): Promise<void> {
    appLogger.info('Stopping collector...');
    this.isRunning = false;

    if (this.page) {
      await this.page.close();
      this.page = null;
    }

    await browserService.closeBrowser();
    appLogger.info('Collector stopped');
  }

  /**
   * Delay helper
   */
  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}

/**
 * Main entry point
 */
async function main() {
  const collector = new SteamPriceCollector();

  // Handle graceful shutdown
  const shutdown = async (signal: string) => {
    appLogger.info(`Received ${signal} - shutting down gracefully...`);
    await collector.stop();
    process.exit(0);
  };

  process.on('SIGINT', () => shutdown('SIGINT'));
  process.on('SIGTERM', () => shutdown('SIGTERM'));

  try {
    // Initialize and start
    await collector.initialize();
    await collector.start();
  } catch (error) {
    appLogger.error({ error }, 'Fatal error');
    await collector.stop();
    process.exit(1);
  }
}

// Run if this is the main module
if (require.main === module) {
  main().catch((error) => {
    logger.error({ error }, 'Unhandled error in main');
    process.exit(1);
  });
}

export { SteamPriceCollector };
