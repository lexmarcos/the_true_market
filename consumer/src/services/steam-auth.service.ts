import { Page } from 'puppeteer';
import { createLogger } from '../utils/logger';
import { SteamSession } from '../types';
import { config } from '../config/config';

const logger = createLogger('SteamAuthService');

export class SteamAuthService {
  private readonly STEAM_LOGIN_URL = 'https://store.steampowered.com/login/';
  private readonly STEAM_MARKET_URL = 'https://steamcommunity.com/market/';

  /**
   * Check if user is logged into Steam
   */
  async checkLoginStatus(page: Page): Promise<boolean> {
    try {
      logger.info('Checking Steam login status...');

      // Navigate to Steam Market to check login
      await page.goto(this.STEAM_MARKET_URL, { waitUntil: 'networkidle2' });

      // Wait a bit for the page to fully load
      await new Promise(resolve => setTimeout(resolve, 2000));

      // Check if user account dropdown exists (indicates logged in)
      const isLoggedIn = await page.evaluate(() => {
        // Check for user account dropdown
        const accountMenu = document.querySelector('#account_pulldown');
        const accountLink = document.querySelector('a[href*="steamcommunity.com/id/"]');
        const loginButton = document.querySelector('a[href*="login"]');

        return (accountMenu !== null || accountLink !== null) && loginButton === null;
      });

      if (isLoggedIn) {
        logger.info('User is logged into Steam');
      } else {
        logger.warn('User is NOT logged into Steam');
      }

      return isLoggedIn;
    } catch (error) {
      logger.error({ error }, 'Error checking login status');
      return false;
    }
  }

  /**
   * Wait for user to complete manual login
   */
  async waitForLogin(page: Page, timeoutMs?: number): Promise<boolean> {
    const timeout = timeoutMs || config.steam.loginTimeoutMs;

    try {
      logger.info(`Navigating to Steam login page...`);
      logger.info(`Please log in manually within ${timeout / 1000} seconds`);

      // Navigate to login page
      await page.goto(this.STEAM_LOGIN_URL, { waitUntil: 'networkidle2' });

      // Wait for login to complete by checking for successful navigation
      const startTime = Date.now();

      while (Date.now() - startTime < timeout) {
        // Check if login was successful
        const isLoggedIn = await this.checkLoginStatus(page);

        if (isLoggedIn) {
          logger.info('Login successful!');
          return true;
        }

        // Wait before checking again
        await new Promise(resolve => setTimeout(resolve, 2000));
      }

      logger.error('Login timeout - user did not complete login');
      return false;
    } catch (error) {
      logger.error({ error }, 'Error during login wait');
      return false;
    }
  }

  /**
   * Get Steam session information
   */
  async getSessionInfo(page: Page): Promise<SteamSession> {
    try {
      const cookies = await page.cookies();
      const steamLoginCookie = cookies.find((c) => c.name === 'steamLoginSecure');
      const sessionIdCookie = cookies.find((c) => c.name === 'sessionid');

      // Extract Steam ID from steamLoginSecure cookie
      let steamId: string | undefined;
      if (steamLoginCookie) {
        const match = steamLoginCookie.value.match(/^(\d+)/);
        if (match) {
          steamId = match[1];
        }
      }

      const isLoggedIn = await this.checkLoginStatus(page);

      return {
        isLoggedIn,
        sessionId: sessionIdCookie?.value,
        steamId,
        cookies: cookies.map((c) => ({
          name: c.name,
          value: c.value,
          domain: c.domain,
        })),
      };
    } catch (error) {
      logger.error({ error }, 'Error getting session info');
      return {
        isLoggedIn: false,
      };
    }
  }

  /**
   * Ensure user is logged in, wait if not
   */
  async ensureLoggedIn(page: Page): Promise<boolean> {
    const isLoggedIn = await this.checkLoginStatus(page);

    if (isLoggedIn) {
      logger.info('Already logged in to Steam');
      return true;
    }

    logger.warn('Not logged in, waiting for manual login...');
    return await this.waitForLogin(page);
  }

  /**
   * Get login cookies for potential re-use
   */
  async getLoginCookies(page: Page): Promise<Array<{ name: string; value: string; domain: string }>> {
    const cookies = await page.cookies();
    return cookies
      .filter((c) => c.domain.includes('steam'))
      .map((c) => ({
        name: c.name,
        value: c.value,
        domain: c.domain,
      }));
  }
}

// Export singleton instance
export const steamAuthService = new SteamAuthService();
