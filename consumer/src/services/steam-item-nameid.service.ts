import axios from 'axios';
import { logger } from '../utils/logger';

interface ItemNameIdMap {
  [key: string]: number;
}

export class SteamItemNameIdService {
  private itemNameIdMap: ItemNameIdMap | null = null;
  private lastUpdateTime: number = 0;
  private readonly CACHE_DURATION_MS = 24 * 60 * 60 * 1000; // 24 horas
  private readonly ITEM_NAMEID_URL = 'https://raw.githubusercontent.com/somespecialone/steam-item-name-ids/refs/heads/master/data/cs2.json';

  constructor() {
    logger.info('SteamItemNameIdService initialized');
  }

  /**
   * Inicializa o cache de item name IDs
   */
  async initialize(): Promise<void> {
    await this.updateCache();
  }

  /**
   * Atualiza o cache de item name IDs se necessário
   */
  private async updateCache(): Promise<void> {
    const now = Date.now();

    // Verifica se precisa atualizar (cache expirado ou não existe)
    if (this.itemNameIdMap && (now - this.lastUpdateTime) < this.CACHE_DURATION_MS) {
      logger.debug('Item name ID cache is still valid, skipping update');
      return;
    }

    try {
      logger.info('Fetching item name IDs from GitHub...');
      const response = await axios.get<ItemNameIdMap>(this.ITEM_NAMEID_URL, {
        timeout: 30000,
        headers: {
          'Accept': 'application/json'
        }
      });

      this.itemNameIdMap = response.data;
      this.lastUpdateTime = now;

      const itemCount = Object.keys(this.itemNameIdMap).length;
      logger.info(`Successfully loaded ${itemCount} item name IDs`);
    } catch (error) {
      logger.error({ error }, 'Failed to fetch item name IDs');

      // Se já tem cache antigo, continua usando
      if (this.itemNameIdMap) {
        logger.warn('Using old cache due to fetch error');
      } else {
        throw new Error('Failed to initialize item name ID cache and no fallback available');
      }
    }
  }

  /**
   * Busca o item_nameid para uma skin específica
   * @param skinName Nome completo da skin incluindo wear (ex: "AK-47 | Midnight Laminate (Field-Tested)")
   * @returns O item_nameid ou null se não encontrado
   */
  async getItemNameId(skinName: string, wear: string): Promise<number | null> {
    // Atualiza o cache se necessário
    await this.updateCache();

    if (!this.itemNameIdMap) {
      logger.error('Item name ID map is not loaded');
      return null;
    }

    // Constrói o nome completo da skin com o wear
    const fullName = this.buildFullSkinName(skinName, wear);

    logger.debug({ skinName, wear, fullName }, 'Looking up item name ID');

    // Busca no mapa
    const itemNameId = this.itemNameIdMap[fullName];

    if (!itemNameId) {
      logger.warn({ fullName }, 'Item name ID not found for skin');

      // Tenta variações comuns
      const alternative = this.tryAlternativeNames(skinName, wear);
      if (alternative) {
        logger.info({ fullName, alternative }, 'Found item name ID using alternative name');
        return alternative;
      }

      return null;
    }

    logger.debug({ fullName, itemNameId }, 'Found item name ID');
    return itemNameId;
  }

  /**
   * Remove qualquer wear existente do nome da skin
   * "AK-47 | Nightwish (Field-Tested)" -> "AK-47 | Nightwish"
   */
  private cleanSkinName(skinName: string): string {
    // Remove anything between parentheses at the end of the string
    return skinName.replace(/\s*\([^)]*\)\s*$/, '').trim();
  }

  /**
   * Constrói o nome completo da skin no formato esperado pelo mapeamento
   */
  private buildFullSkinName(skinName: string, wear: string): string {
    // Limpa o nome da skin removendo qualquer wear existente
    const cleanedName = this.cleanSkinName(skinName);

    // Mapeia os valores de wear para o formato da Steam
    const wearMap: { [key: string]: string } = {
      'FACTORY_NEW': 'Factory New',
      'MINIMAL_WEAR': 'Minimal Wear',
      'FIELD_TESTED': 'Field-Tested',
      'WELL_WORN': 'Well-Worn',
      'BATTLE_SCARRED': 'Battle-Scarred'
    };

    const wearText = wearMap[wear] || wear;
    return `${cleanedName} (${wearText})`;
  }

  /**
   * Tenta encontrar o item usando nomes alternativos
   */
  private tryAlternativeNames(skinName: string, wear: string): number | null {
    if (!this.itemNameIdMap) return null;

    // Tenta com diferentes formatos de wear
    const wearVariations = [
      'Factory New',
      'Minimal Wear',
      'Field-Tested',
      'Well-Worn',
      'Battle-Scarred',
      'FN',
      'MW',
      'FT',
      'WW',
      'BS'
    ];

    for (const wearVar of wearVariations) {
      const testName = `${skinName} (${wearVar})`;
      if (this.itemNameIdMap[testName]) {
        return this.itemNameIdMap[testName];
      }
    }

    return null;
  }

  /**
   * Retorna estatísticas do cache
   */
  getCacheStats(): { itemCount: number; lastUpdate: Date | null; isValid: boolean } {
    return {
      itemCount: this.itemNameIdMap ? Object.keys(this.itemNameIdMap).length : 0,
      lastUpdate: this.lastUpdateTime ? new Date(this.lastUpdateTime) : null,
      isValid: this.itemNameIdMap !== null && (Date.now() - this.lastUpdateTime) < this.CACHE_DURATION_MS
    };
  }
}
