/**
 * Format currency from cents to display format
 * @param cents - Amount in cents
 * @param currency - Currency code (USD, BRL, etc.)
 * @returns Formatted currency string
 */
export function formatCurrency(
  cents: number | null | undefined,
  currency: string = "USD"
): string {
  if (cents === null || cents === undefined) {
    return "N/A";
  }

  const amount = cents / 100;

  const currencyMap: Record<string, string> = {
    USD: "en-US",
    BRL: "pt-BR",
    EUR: "de-DE",
  };

  const locale = currencyMap[currency] || "en-US";

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Format percentage value
 * @param value - Percentage value in cents (e.g., 1451 for 14.51%)
 * @returns Formatted percentage string
 */
export function formatPercentage(value: number | null | undefined): string {
  if (value === null || value === undefined) {
    return "N/A";
  }

  // Convert from cents to percentage (1451 -> 14.51%)
  const percentage = value / 100;
  return `${percentage.toFixed(2)}%`;
}

/**
 * Format ISO date string to localized date
 * @param isoDate - ISO 8601 date string
 * @param locale - Locale string (default: 'pt-BR')
 * @returns Formatted date string
 */
export function formatDate(
  isoDate: string | null | undefined,
  locale: string = "pt-BR"
): string {
  if (!isoDate) {
    return "N/A";
  }

  try {
    const date = new Date(isoDate);
    return new Intl.DateTimeFormat(locale, {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  } catch {
    return "Invalid Date";
  }
}

/**
 * Format relative time (e.g., "2 hours ago")
 * @param isoDate - ISO 8601 date string
 * @param locale - Locale string (default: 'pt-BR')
 * @returns Formatted relative time string
 */
export function formatRelativeTime(
  isoDate: string | null | undefined,
  locale: string = "pt-BR"
): string {
  if (!isoDate) {
    return "N/A";
  }

  try {
    const date = new Date(isoDate);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    const intervals = {
      year: 31536000,
      month: 2592000,
      week: 604800,
      day: 86400,
      hour: 3600,
      minute: 60,
    };

    for (const [unit, seconds] of Object.entries(intervals)) {
      const interval = Math.floor(diffInSeconds / seconds);
      if (interval >= 1) {
        const rtf = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });
        return rtf.format(
          -interval,
          unit as Intl.RelativeTimeFormatUnit
        );
      }
    }

    return "agora";
  } catch {
    return "Invalid Date";
  }
}

/**
 * Get wear display name
 * @param wear - Wear enum value
 * @returns Display name
 */
export function formatWear(wear: string): string {
  const wearMap: Record<string, string> = {
    FACTORY_NEW: "Factory New",
    MINIMAL_WEAR: "Minimal Wear",
    FIELD_TESTED: "Field-Tested",
    WELL_WORN: "Well-Worn",
    BATTLE_SCARRED: "Battle-Scarred",
  };

  return wearMap[wear] || wear;
}

/**
 * Get market source display name
 * @param source - Market source enum value
 * @returns Display name
 */
export function formatMarketSource(source: string): string {
  const sourceMap: Record<string, string> = {
    BITSKINS: "BitSkins",
    DASHSKINS: "DashSkins",
    STEAM: "Steam Market",
  };

  return sourceMap[source] || source;
}
