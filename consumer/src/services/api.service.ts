import axios, { AxiosInstance, AxiosError } from 'axios';
import { createLogger } from '../utils/logger';
import { config } from '../config/config';
import { ItemData, ApiResponse } from '../types';

const logger = createLogger('ApiService');

export class ApiService {
  private client: AxiosInstance;
  private readonly MAX_RETRIES = 3;
  private readonly RETRY_DELAY_MS = 2000;

  constructor() {
    this.client = axios.create({
      baseURL: config.api.url,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${config.api.key}`,
      },
    });

    // Request interceptor for logging
    this.client.interceptors.request.use(
      (config) => {
        logger.debug({
          method: config.method,
          url: config.url,
        }, 'API Request');
        return config;
      },
      (error) => {
        logger.error({ error }, 'API Request Error');
        return Promise.reject(error);
      }
    );

    // Response interceptor for logging
    this.client.interceptors.response.use(
      (response) => {
        logger.debug({
          status: response.status,
          url: response.config.url,
        }, 'API Response');
        return response;
      },
      (error) => {
        logger.error({
          status: error.response?.status,
          url: error.config?.url,
          message: error.message,
        }, 'API Response Error');
        return Promise.reject(error);
      }
    );
  }

  /**
   * Send item data to API
   */
  async sendItemData(item: ItemData): Promise<ApiResponse> {
    return this.withRetry(async () => {
      try {
        logger.info({
          name: item.name,
          price: item.price,
        }, 'Sending item data to API');

        const response = await this.client.post<ApiResponse>('/items', item);

        logger.info({
          name: item.name,
          success: response.data.success,
        }, 'Item data sent successfully');

        return response.data;
      } catch (error) {
        this.handleError('sendItemData', error);
        throw error;
      }
    });
  }

  /**
   * Send multiple items data to API
   */
  async sendBulkItemData(items: ItemData[]): Promise<ApiResponse> {
    return this.withRetry(async () => {
      try {
        logger.info(`Sending ${items.length} items to API`);

        const response = await this.client.post<ApiResponse>('/items/bulk', {
          items,
        });

        logger.info({
          count: items.length,
          success: response.data.success,
        }, 'Bulk item data sent successfully');

        return response.data;
      } catch (error) {
        this.handleError('sendBulkItemData', error);
        throw error;
      }
    });
  }

  /**
   * Health check for API
   */
  async healthCheck(): Promise<boolean> {
    try {
      logger.info('Performing API health check...');

      const response = await this.client.get<ApiResponse>('/health');

      const isHealthy = response.status === 200 && response.data.success;

      if (isHealthy) {
        logger.info('API health check passed');
      } else {
        logger.warn({ response: response.data }, 'API health check failed');
      }

      return isHealthy;
    } catch (error) {
      logger.error({ error }, 'API health check failed');
      return false;
    }
  }

  /**
   * Retry wrapper with exponential backoff
   */
  private async withRetry<T>(operation: () => Promise<T>): Promise<T> {
    let lastError: Error | null = null;

    for (let attempt = 1; attempt <= this.MAX_RETRIES; attempt++) {
      try {
        return await operation();
      } catch (error) {
        lastError = error as Error;

        if (attempt < this.MAX_RETRIES) {
          const delay = this.RETRY_DELAY_MS * Math.pow(2, attempt - 1); // Exponential backoff
          logger.warn({
            error: lastError.message,
          }, `Retry attempt ${attempt}/${this.MAX_RETRIES} after ${delay}ms`);
          await this.delay(delay);
        }
      }
    }

    logger.error(`All ${this.MAX_RETRIES} retry attempts failed`);
    throw lastError;
  }

  /**
   * Handle and log errors
   */
  private handleError(operation: string, error: unknown): void {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      logger.error({
        status: axiosError.response?.status,
        statusText: axiosError.response?.statusText,
        message: axiosError.message,
        data: axiosError.response?.data,
      }, `${operation} failed`);
    } else {
      logger.error({
        error: error instanceof Error ? error.message : 'Unknown error',
      }, `${operation} failed`);
    }
  }

  /**
   * Delay helper
   */
  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  /**
   * Update API key
   */
  updateApiKey(apiKey: string): void {
    this.client.defaults.headers.common['Authorization'] = `Bearer ${apiKey}`;
    logger.info('API key updated');
  }

  /**
   * Get current API configuration
   */
  getConfig() {
    return {
      baseURL: this.client.defaults.baseURL,
      timeout: this.client.defaults.timeout,
    };
  }
}

// Export singleton instance
export const apiService = new ApiService();
