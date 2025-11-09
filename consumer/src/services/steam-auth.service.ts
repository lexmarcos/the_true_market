import { Page } from 'puppeteer';
import { createLogger } from '../utils/logger';
import { SteamSession } from '../types';
import { config } from '../config/config';

const logger = createLogger('SteamAuthService');

export class SteamAuthService {
  private readonly STEAM_LOGIN_URL = 'https://store.steampowered.com/login/';
  private readonly STEAM_MARKET_URL = 'https://steamcommunity.com/market/';

  /**
   * Check if user is logged into Steam by verifying cookies
   * This method does NOT navigate to any page
   */
  private async checkLoginStatusByCookies(page: Page): Promise<boolean> {
    try {
      const cookies = await page.cookies();
      const steamLoginCookie = cookies.find((c) => c.name === 'steamLoginSecure');
      const sessionIdCookie = cookies.find((c) => c.name === 'sessionid');

      // User is logged in if both critical cookies exist
      const isLoggedIn = !!(steamLoginCookie && sessionIdCookie);

      if (isLoggedIn) {
        logger.info('User is logged into Steam (verified by cookies)');
      } else {
        logger.warn('User is NOT logged into Steam (missing login cookies)');
      }

      return isLoggedIn;
    } catch (error) {
      logger.error({ error }, 'Error checking login status by cookies');
      return false;
    }
  }

  /**
   * Check if user is logged into Steam
   * Uses cookie verification to avoid unnecessary navigation
   */
  async checkLoginStatus(page: Page): Promise<boolean> {
    return this.checkLoginStatusByCookies(page);
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
