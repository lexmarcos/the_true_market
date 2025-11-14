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
                className="overflow-hidden border-0 bg-white/95 dark:bg-slate-900/60 shadow-xl ring-1 ring-slate-200/80 dark:ring-slate-800/70 transition-all duration-500 hover:-translate-y-1 hover:shadow-2xl"
              >
                <CardHeader className="pb-0 space-y-4">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">
                        Destaque da semana
                      </p>
                      <h3 className="mt-2 font-semibold text-xl text-slate-900 dark:text-slate-100 leading-snug">
                        {skin.skinName}
                      </h3>
                    </div>
                    <span className="rounded-full bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-200 px-3 py-1 text-xs font-semibold border border-emerald-100/80 dark:border-emerald-400/30">
                      {skin.wearDisplay}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.3em] text-slate-500 dark:text-slate-400">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                    Oportunidade monitorada
                  </div>
                </CardHeader>

                <CardContent className="space-y-5 px-5 pb-5 pt-4">
                  <section className="rounded-2xl border border-slate-100/80 dark:border-slate-800/70 bg-gradient-to-br from-sky-50 to-indigo-50 dark:from-slate-900 dark:to-slate-800 p-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-[11px] font-semibold uppercase tracking-[0.3em] text-slate-500 dark:text-slate-400">
                          Oferta no marketplace
                        </p>
                        <p className="text-sm text-slate-500 dark:text-slate-400">
                          {skin.marketSourceDisplay}
                        </p>
                      </div>
                      <p className="text-3xl font-semibold text-slate-900 dark:text-white">
                        {skin.marketPrice}
                      </p>
                    </div>
                  </section>

                  {skin.hasHistory && (
                    <section className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-white/80 dark:bg-slate-900/40 p-4 space-y-3">
                      <p className="text-[11px] font-semibold uppercase tracking-[0.25em] text-slate-500 dark:text-slate-400">
                        Referências oficiais da Steam
                      </p>
                      <div className="grid gap-3 sm:grid-cols-2">
                        <div className="rounded-xl bg-slate-50 dark:bg-slate-800/70 border border-slate-100 dark:border-slate-700 p-3">
                          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                            Média das últimas 10 vendas
                          </p>
                          <p className="text-base font-medium text-slate-900 dark:text-slate-100">
                            {skin.steamAveragePrice}
                          </p>
                        </div>
                        {skin.lastSalePrice !== "N/A" && (
                          <div className="rounded-xl bg-slate-50 dark:bg-slate-800/70 border border-slate-100 dark:border-slate-700 p-3">
                            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                              Última venda registrada
                            </p>
                            <p className="text-base font-medium text-slate-900 dark:text-slate-100">
                              {skin.lastSalePrice}
                            </p>
                          </div>
                        )}
                        {skin.lowestBuyOrderPrice !== "N/A" && (
                          <div className="rounded-xl bg-slate-50 dark:bg-slate-800/70 border border-slate-100 dark:border-slate-700 p-3 sm:col-span-2">
                            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                              Menor ordem de compra ativa
                            </p>
                            <p className="text-base font-medium text-slate-900 dark:text-slate-100">
                              {skin.lowestBuyOrderPrice}
                            </p>
                          </div>
                        )}
                      </div>
                    </section>
                  )}

                  <div className="space-y-2">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.3em] text-slate-500 dark:text-slate-400">
                      Indicadores rápidos
                    </p>
                    <div className="flex flex-wrap gap-2">
                      <Badge variant={skin.profitBadgeVariant} className="text-[11px] font-semibold tracking-tight px-3 py-1">
                        Margem média Steam · {skin.profitPercentage}
                      </Badge>
                      {skin.hasHistory && skin.profitPercentageVsLastSale !== "N/A" && (
                        <Badge variant={skin.profitVsLastSaleBadgeVariant} className="text-[11px] font-semibold tracking-tight px-3 py-1">
                          Margem última venda · {skin.profitPercentageVsLastSale}
                        </Badge>
                      )}
                      {skin.hasHistory && skin.profitPercentageVsLowestBuyOrder !== "N/A" && (
                        <Badge variant={skin.profitVsLowestBuyOrderBadgeVariant} className="text-[11px] font-semibold tracking-tight px-3 py-1">
                          Margem menor ordem · {skin.profitPercentageVsLowestBuyOrder}
                        </Badge>
                      )}
                      <Badge variant={skin.discountBadgeVariant} className="text-[11px] font-semibold tracking-tight px-3 py-1">
                        Desconto marketplace · {skin.discountPercentage}
                      </Badge>
                    </div>
                  </div>

                  {skin.hasHistory && (
                    <section className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-gradient-to-br from-emerald-50 to-teal-50 dark:from-slate-900 dark:to-slate-800 p-4 space-y-3">
                      <p className="text-[11px] font-semibold uppercase tracking-[0.25em] text-emerald-700 dark:text-emerald-300">
                        Projeção de ganho líquido
                      </p>
                      <div className="space-y-3">
                        <div className="flex flex-col gap-1">
                          <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                            Ganho em relação à média das últimas 10 vendas na Steam
                          </span>
                          <span className={`text-xl font-semibold ${skin.expectedGainColorClass}`}>
                            {skin.expectedGainUsd}
                          </span>
                        </div>
                        {skin.expectedGainVsLastSale !== "N/A" && (
                          <div className="flex flex-col gap-1">
                            <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                              Ganho em relação à última venda registrada na Steam
                            </span>
                            <span className={`text-xl font-semibold ${skin.expectedGainVsLastSaleColorClass}`}>
                              {skin.expectedGainVsLastSale}
                            </span>
                          </div>
                        )}
                        {skin.expectedGainVsLowestBuyOrder !== "N/A" && (
                          <div className="flex flex-col gap-1">
                            <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                              Ganho em relação à menor ordem de compra ativa na Steam
                            </span>
                            <span className={`text-xl font-semibold ${skin.expectedGainVsLowestBuyOrderColorClass}`}>
                              {skin.expectedGainVsLowestBuyOrder}
                            </span>
                          </div>
                        )}
                      </div>
                    </section>
                  )}
                </CardContent>

                <CardFooter className="bg-gradient-to-r from-slate-50 via-indigo-50 to-purple-50 dark:from-slate-900/60 dark:via-slate-900/40 dark:to-slate-900/60 border-t border-slate-100 dark:border-slate-800 pt-4 pb-4 flex-col gap-3">
                  {/* Action Buttons */}
                  <div className="flex gap-2 w-full">
                    <Button
                      asChild
                      variant="outline"
                      size="sm"
                      className="flex-1 gap-2 border-slate-200/70 bg-white/80 hover:bg-white"
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
                      className="flex-1 gap-2 border-slate-200/70 bg-white/80 hover:bg-white"
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
                  <p className="text-xs font-medium text-slate-600 dark:text-slate-300 w-full text-center">
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
