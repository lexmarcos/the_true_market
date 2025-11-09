import puppeteer, { Browser, Page } from 'puppeteer';
import { config } from '../config/config';
import { createLogger } from '../utils/logger';
import { BrowserOptions } from '../types';
import fs from 'fs';

const logger = createLogger('BrowserService');

export class BrowserService {
  private browser: Browser | null = null;
  private pages: Page[] = [];

  /**
   * Initialize browser with persistent profile
   */
  async initBrowser(options?: Partial<BrowserOptions>): Promise<Browser> {
    try {
      logger.info('Initializing browser with persistent profile...');

      // Ensure browser data directory exists
      const userDataDir = options?.userDataDir || config.browser.dataDir;
      if (!fs.existsSync(userDataDir)) {
        fs.mkdirSync(userDataDir, { recursive: true });
        logger.info(`Created browser data directory: ${userDataDir}`);
      }

      const browserOptions = {
        headless: options?.headless ?? config.browser.headless,
        userDataDir,
        defaultViewport: options?.viewport || { width: 1920, height: 1080 },
        args: [
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--disable-dev-shm-usage',
          '--disable-blink-features=AutomationControlled',
        ],
      };

      this.browser = await puppeteer.launch(browserOptions);

      logger.info({
        headless: browserOptions.headless,
        userDataDir: browserOptions.userDataDir,
      }, 'Browser initialized successfully');

      return this.browser;
    } catch (error) {
      logger.error({ error }, 'Failed to initialize browser');
      throw error;
    }
  }

  /**
   * Create a new page with default settings
   */
  async createPage(): Promise<Page> {
    if (!this.browser) {
      throw new Error('Browser not initialized. Call initBrowser() first.');
    }

    try {
      const page = await this.browser.newPage();

      // Set default timeout
      page.setDefaultTimeout(30000);
      page.setDefaultNavigationTimeout(30000);

      // Set user agent to avoid detection
      await page.setUserAgent(
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36'
      );

      // Additional stealth settings
      await page.evaluateOnNewDocument(() => {
        // Override the navigator.webdriver property
        Object.defineProperty(navigator, 'webdriver', {
          get: () => false,
        });

        // Override the navigator.plugins property
        Object.defineProperty(navigator, 'plugins', {
          get: () => [1, 2, 3, 4, 5],
        });

        // Override the navigator.languages property
        Object.defineProperty(navigator, 'languages', {
          get: () => ['en-US', 'en'],
        });
      });

      this.pages.push(page);
      logger.info('New page created');

      return page;
    } catch (error) {
      logger.error({ error }, 'Failed to create page');
      throw error;
    }
  }

  /**
   * Navigate to URL with retry logic
   */
  async navigateTo(page: Page, url: string, options?: { waitUntil?: 'load' | 'domcontentloaded' | 'networkidle0' | 'networkidle2'; retries?: number }): Promise<void> {
    const waitUntil = options?.waitUntil || 'networkidle2';
    const maxRetries = options?.retries || 3;
    let lastError: Error | null = null;

    for (let i = 0; i < maxRetries; i++) {
      try {
        logger.info(`Navigating to ${url} (attempt ${i + 1}/${maxRetries})`);
        await page.goto(url, { waitUntil });
        logger.info(`Successfully navigated to ${url}`);
        return;
      } catch (error) {
        lastError = error as Error;
        logger.warn({ error, url }, `Navigation attempt ${i + 1} failed`);
        if (i < maxRetries - 1) {
          await this.delay(2000 * (i + 1)); // Exponential backoff
        }
      }
    }

    throw new Error(`Failed to navigate to ${url} after ${maxRetries} attempts: ${lastError?.message}`);
  }

  /**
   * Get browser instance
   */
  getBrowser(): Browser {
    if (!this.browser) {
      throw new Error('Browser not initialized. Call initBrowser() first.');
    }
    return this.browser;
  }

  /**
   * Check if browser is initialized
   */
  isInitialized(): boolean {
    return this.browser !== null && this.browser.connected;
  }

  /**
   * Close all pages
   */
  async closeAllPages(): Promise<void> {
    logger.info('Closing all pages...');
    for (const page of this.pages) {
      try {
        await page.close();
      } catch (error) {
        logger.warn({ error }, 'Error closing page');
      }
    }
    this.pages = [];
  }

  /**
   * Close browser and cleanup
   */
  async closeBrowser(): Promise<void> {
    if (!this.browser) {
      logger.warn('Browser not initialized, nothing to close');
      return;
    }

    try {
      logger.info('Closing browser...');
      await this.closeAllPages();
      await this.browser.close();
      this.browser = null;
      logger.info('Browser closed successfully');
    } catch (error) {
      logger.error({ error }, 'Error closing browser');
      throw error;
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
export const browserService = new BrowserService();
