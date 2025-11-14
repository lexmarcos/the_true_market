import axios from 'axios';
import { logger } from '../utils/logger';

interface BuyOrderHistogramResponse {
  success: number;
  sell_order_table?: string;
  sell_order_summary?: string;
  buy_order_table?: string;
  buy_order_summary?: string;
  highest_buy_order?: string;
  lowest_sell_order?: string;
  buy_order_graph?: Array<[number, number, string]>; // [preço, quantidade, descrição]
  sell_order_graph?: Array<[number, number, string]>;
  graph_max_y?: number;
  graph_min_x?: number;
  graph_max_x?: number;
  price_prefix?: string;
  price_suffix?: string;
}

export class SteamBuyOrdersService {
  private readonly BASE_URL = 'https://steamcommunity.com/market/itemordershistogram';

  constructor() {
    logger.info('SteamBuyOrdersService initialized');
  }

  /**
   * Busca a menor ordem de compra (highest buy order) para um item
   * @param itemNameId O item_nameid da skin
   * @returns O preço da maior ordem de compra em centavos USD, ou null se não encontrado
   */
  async getLowestBuyOrder(itemNameId: number): Promise<number | null> {
    try {
      logger.debug({ itemNameId }, 'Fetching buy orders for item');

      const url = this.buildUrl(itemNameId);
      const response = await axios.get<BuyOrderHistogramResponse>(url, {
        timeout: 30000,
        headers: {
          'Accept': 'application/json',
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
      });

      if (!response.data.success) {
        logger.warn({ itemNameId }, 'Buy order request was not successful');
        return null;
      }

      const buyOrderGraph = response.data.buy_order_graph;

      if (!buyOrderGraph || buyOrderGraph.length === 0) {
        logger.warn({ itemNameId }, 'No buy orders found for item');
        return null;
      }

      // O primeiro item do array é a maior ordem de compra (highest buy order)
      const highestBuyOrder = buyOrderGraph[0];
      const priceUsd = highestBuyOrder[0]; // Preço em USD
      const quantity = highestBuyOrder[1];

      logger.debug({
        itemNameId,
        priceUsd,
        quantity,
        description: highestBuyOrder[2]
      }, 'Found highest buy order');

      // Converte para centavos e arredonda
      const priceInCents = Math.round(priceUsd * 100);

      logger.info({
        itemNameId,
        priceUsd,
        priceInCents,
        quantity
      }, 'Successfully retrieved highest buy order');

      return priceInCents;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        logger.error({
          itemNameId,
          status: error.response?.status,
          statusText: error.response?.statusText,
          message: error.message
        }, 'Failed to fetch buy orders');
      } else {
        logger.error({ itemNameId, error }, 'Failed to fetch buy orders');
      }

      return null;
    }
  }

  /**
   * Constrói a URL para o endpoint de buy orders
   */
  private buildUrl(itemNameId: number): string {
    const params = new URLSearchParams({
      country: 'PK',
      language: 'english',
      currency: '1', // USD
      item_nameid: itemNameId.toString(),
      two_factor: '0',
      norender: '1'
    });

    return `${this.BASE_URL}?${params.toString()}`;
  }

  /**
   * Busca informações detalhadas do histogram (buy e sell orders)
   * Método auxiliar para debug/análise
   */
  async getFullHistogram(itemNameId: number): Promise<BuyOrderHistogramResponse | null> {
    try {
      const url = this.buildUrl(itemNameId);
      const response = await axios.get<BuyOrderHistogramResponse>(url, {
        timeout: 30000,
        headers: {
          'Accept': 'application/json',
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
      });

      return response.data;
    } catch (error) {
      logger.error({ itemNameId, error }, 'Failed to fetch full histogram');
      return null;
    }
  }
}
