import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { AlertCircle, RefreshCw, TrendingUp, TrendingDown, ExternalLink } from "lucide-react";
import { SkinCardData } from "../types/profitable-skins.types";

interface HomeViewProps {
  skins: SkinCardData[];
  isLoading: boolean;
  error: string | null;
  refetch: () => void;
  totalCount: number;
  hasProfitableSkins: boolean;
  siteName: string;
  sortBy: "profit" | "discount" | "gain";
  order: "asc" | "desc";
  onSortByChange: (value: "profit" | "discount" | "gain") => void;
  onOrderChange: (value: "asc" | "desc") => void;
}

/**
 * Pure presentation component for the home page
 * NO LOGIC - Only JSX rendering using props
 */
export function HomeView({
  skins,
  isLoading,
  error,
  refetch,
  totalCount,
  hasProfitableSkins,
  siteName,
  sortBy,
  order,
  onSortByChange,
  onOrderChange,
}: HomeViewProps) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <header className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                {siteName}
              </h1>
              <p className="text-slate-600 dark:text-slate-400 mt-2">
                Encontre as melhores oportunidades de lucro em skins CS2
              </p>
            </div>
            <Button
              onClick={refetch}
              disabled={isLoading}
              variant="outline"
              size="lg"
              className="gap-2"
            >
              <RefreshCw className={isLoading ? "animate-spin" : ""} size={16} />
              Atualizar
            </Button>
          </div>

          {/* Sorting Controls */}
          <div className="flex flex-wrap items-center gap-4 p-4 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Ordenar por:
              </span>
              <Select value={sortBy} onValueChange={onSortByChange}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="profit">Lucro</SelectItem>
                  <SelectItem value="discount">Desconto</SelectItem>
                  <SelectItem value="gain">Ganho</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Ordem:
              </span>
              <Select value={order} onValueChange={onOrderChange}>
                <SelectTrigger className="w-[160px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="desc">Maior para Menor</SelectItem>
                  <SelectItem value="asc">Menor para Maior</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Stats */}
          {hasProfitableSkins && (
            <div className="mt-6 bg-white dark:bg-slate-800 rounded-lg p-4 shadow-sm border border-slate-200 dark:border-slate-700">
              <div className="flex items-center gap-2">
                <TrendingUp className="text-green-600" size={20} />
                <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                  {totalCount} skins lucrativas encontradas
                </span>
              </div>
            </div>
          )}
        </header>

        {/* Loading State */}
        {isLoading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: 8 }).map((_, index) => (
              <Card key={index} className="overflow-hidden">
                <CardHeader className="pb-4">
                  <Skeleton className="h-6 w-3/4" />
                  <Skeleton className="h-4 w-1/2 mt-2" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-4 w-full mb-2" />
                  <Skeleton className="h-4 w-full mb-2" />
                  <Skeleton className="h-4 w-2/3" />
                </CardContent>
                <CardFooter>
                  <Skeleton className="h-8 w-full" />
                </CardFooter>
              </Card>
            ))}
          </div>
        )}

        {/* Error State */}
        {error && (
          <Card className="bg-red-50 dark:bg-red-950 border-red-200 dark:border-red-800">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3 text-red-700 dark:text-red-300">
                <AlertCircle size={24} />
                <div>
                  <p className="font-semibold">Erro ao carregar skins</p>
                  <p className="text-sm mt-1">{error}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Empty State */}
        {!isLoading && !error && !hasProfitableSkins && (
          <Card className="bg-slate-50 dark:bg-slate-800">
            <CardContent className="pt-12 pb-12 text-center">
              <TrendingDown className="mx-auto mb-4 text-slate-400" size={48} />
              <h3 className="text-xl font-semibold text-slate-700 dark:text-slate-300 mb-2">
                Nenhuma skin encontrada
              </h3>
              <p className="text-slate-500 dark:text-slate-400">
                Não há skins lucrativas disponíveis no momento.
              </p>
            </CardContent>
          </Card>
        )}

        {/* Skins Grid */}
        {!isLoading && !error && hasProfitableSkins && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {skins.map((skin) => (
              <Card
                key={skin.skinId}
                className="overflow-hidden hover:shadow-lg transition-all duration-300 hover:-translate-y-1 border-slate-200 dark:border-slate-700"
              >
                <CardHeader className="pb-4">
                  <h3 className="font-semibold text-lg text-slate-900 dark:text-slate-100 line-clamp-2">
                    {skin.skinName}
                  </h3>
                  <p className="text-sm text-slate-600 dark:text-slate-400">
                    {skin.wearDisplay}
                  </p>
                </CardHeader>

                <CardContent className="space-y-3">
                  {/* Market Info */}
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-slate-500 dark:text-slate-400">
                      {skin.marketSourceDisplay}
                    </span>
                    <span className="text-sm font-bold text-slate-900 dark:text-slate-100">
                      {skin.marketPrice}
                    </span>
                  </div>

                  {/* Steam Price */}
                  {skin.hasHistory && (
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-slate-500 dark:text-slate-400">
                        Steam
                      </span>
                      <span className="text-sm text-slate-700 dark:text-slate-300">
                        {skin.steamAveragePrice}
                      </span>
                    </div>
                  )}

                  {/* Badges */}
                  <div className="flex gap-2 flex-wrap">
                    <Badge variant={skin.profitBadgeVariant} className="text-xs">
                      Lucro: {skin.profitPercentage}
                    </Badge>
                    <Badge variant={skin.discountBadgeVariant} className="text-xs">
                      Desc: {skin.discountPercentage}
                    </Badge>
                  </div>

                  {/* Expected Gain/Loss */}
                  {skin.hasHistory && (
                    <div className="pt-2 border-t border-slate-200 dark:border-slate-700">
                      <div className="flex items-center justify-between">
                        <span className="text-xs text-slate-500 dark:text-slate-400">
                          {skin.expectedGainLabel}
                        </span>
                        <span className={`text-sm font-semibold ${skin.expectedGainColorClass}`}>
                          {skin.expectedGainUsd}
                        </span>
                      </div>
                    </div>
                  )}
                </CardContent>

                <CardFooter className="bg-slate-50 dark:bg-slate-800/50 pt-3 pb-3 flex-col gap-3">
                  {/* Action Buttons */}
                  <div className="flex gap-2 w-full">
                    <Button
                      asChild
                      variant="outline"
                      size="sm"
                      className="flex-1 gap-2"
                    >
                      <a
                        href={skin.link}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        <ExternalLink size={14} />
                        Marketplace
                      </a>
                    </Button>
                    <Button
                      asChild
                      variant="outline"
                      size="sm"
                      className="flex-1 gap-2"
                    >
                      <a
                        href={skin.steamMarketLink}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        <ExternalLink size={14} />
                        Steam
                      </a>
                    </Button>
                  </div>

                  {/* Last Updated */}
                  <p className="text-xs text-slate-500 dark:text-slate-400 w-full text-center">
                    Atualizado {skin.lastUpdatedRelative}
                  </p>
                </CardFooter>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
