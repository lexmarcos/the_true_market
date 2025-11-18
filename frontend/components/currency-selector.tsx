"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Currency, useCurrency } from "@/lib/currency-context";

/**
 * CurrencySelector Component
 * Allows users to select their preferred currency (USD, BRL, EUR)
 */
export function CurrencySelector() {
  const { selectedCurrency, setSelectedCurrency } = useCurrency();

  const currencies: { value: Currency; label: string; symbol: string }[] = [
    { value: "USD", label: "USD", symbol: "$" },
    { value: "BRL", label: "BRL", symbol: "R$" },
    { value: "EUR", label: "EUR", symbol: "€" },
  ];

  return (
    <Select
      value={selectedCurrency}
      onValueChange={(value) => setSelectedCurrency(value as Currency)}
    >
      <SelectTrigger className="w-[120px]">
        <SelectValue placeholder="Currency" />
      </SelectTrigger>
      <SelectContent>
        {currencies.map((currency) => (
          <SelectItem key={currency.value} value={currency.value}>
            <span className="flex items-center gap-2">
              <span className="font-semibold">{currency.symbol}</span>
              <span>{currency.label}</span>
            </span>
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
