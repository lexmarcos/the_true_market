import { Page } from 'puppeteer';
import { browserService } from './services/browser.service';
import { steamAuthService } from './services/steam-auth.service';
import { steamHistoryService } from './services/steam-history.service';
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
   * Start the history task polling loop
   */
  async start(): Promise<void> {
    if (!this.page) {
      throw new Error('Collector not initialized. Call initialize() first.');
    }

    this.isRunning = true;
    appLogger.info('Starting history task polling loop (every 30 seconds)...');

    while (this.isRunning) {
      try {
        await this.processHistoryTasks();

        // Wait 30 seconds before next poll
        appLogger.info('Waiting 30 seconds until next poll...');
        await this.delay(30000);
      } catch (error) {
        appLogger.error({ error }, 'Error in polling loop');
        appLogger.info('Retrying in 30 seconds...');
        await this.delay(30000);
      }
    }
  }

  /**
   * Process history update tasks from API
   */
  private async processHistoryTasks(): Promise<void> {
    if (!this.page) return;

    try {
      appLogger.info('=== Fetching History Tasks ===');

      // Get tasks from API
      const tasks = await apiService.getHistoryUpdateTasks();

      // Filter WAITING tasks
      const waitingTasks = tasks.filter(task => task.status === 'WAITING');

      if (waitingTasks.length === 0) {
        appLogger.info('No WAITING tasks found');
        return;
      }

      appLogger.info({ totalTasks: tasks.length, waitingTasks: waitingTasks.length }, 'Tasks fetched');

      // Process tasks in FIFO order
      for (const task of waitingTasks) {
        try {
          appLogger.info({
            taskId: task.id,
            skinName: task.skinName,
            wear: task.wear,
          }, 'Processing task');

          // Fetch price history from Steam
          const priceHistory = await steamHistoryService.getPriceHistory(
            this.page,
            task.skinName,
            task.wear
          );

          if (!priceHistory) {
            appLogger.warn({ taskId: task.id }, 'Failed to fetch price history');
            continue;
          }

          // Calculate average of last 10 prices
          const averagePrice = steamHistoryService.calculateAveragePrice(priceHistory);

          if (averagePrice === null) {
            appLogger.warn({ taskId: task.id }, 'Failed to calculate average price');
            continue;
          }

          // Complete task via API
          const result = await apiService.completeHistoryTask(task.id, {
            skinName: task.skinName,
            wear: task.wear,
            averagePrice,
          });

          appLogger.info({
            taskId: task.id,
            averagePrice,
            success: result.success,
          }, 'Task completed successfully');

          // Rate limiting between tasks
          await this.delay(config.scraping.rateLimitDelayMs);

        } catch (error) {
          appLogger.error({ error, taskId: task.id }, 'Error processing task');
          // Continue to next task
        }
      }

      appLogger.info('=== All Tasks Processed ===');
    } catch (error) {
      appLogger.error({ error }, 'Error processing history tasks');
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
