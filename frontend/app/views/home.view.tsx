import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { WearIndicator } from "@/components/ui/wear-indicator";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  AlertCircle,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  ExternalLink,
  ChartArea,
} from "lucide-react";
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
  activeIndicatorsSkinId: string | null;
  onIndicatorsOpen: (skinId: string) => void;
  onIndicatorsClose: () => void;
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
  activeIndicatorsSkinId,
  onIndicatorsOpen,
  onIndicatorsClose,
}: HomeViewProps) {
  return (
    <div className="min-h-screen bg-linear-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <header className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-4xl font-bold bg-linear-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
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
              <RefreshCw
                className={isLoading ? "animate-spin" : ""}
                size={16}
              />
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
                <SelectTrigger className="w-44">
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
                <SelectTrigger className="w-40">
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
                  <div className="flex flex-col gap-2">
                    <h3 className="mt-0 font-semibold text-xl text-slate-900 dark:text-slate-100 leading-snug">
                      {skin.skinName}
                    </h3>
                    <div className="">
                      <WearIndicator
                        label={skin.wearDisplay}
                        valueLabel={skin.wearValueDisplay}
                        normalizedValue={skin.wearValueNormalized}
                      />
                    </div>
                  </div>
                </CardHeader>

                <CardContent className="space-y-5 px-5 pb-5">
                  <section className="rounded-2xl border border-slate-100/80 dark:border-slate-800/70 bg-linear-to-br from-sky-50 to-indigo-50 dark:from-slate-900 dark:to-slate-800 p-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <img
                          src={`/${skin.marketSourceDisplay}.svg`}
                          alt={skin.marketSourceDisplay}
                          className="h-6 w-32"
                        />
                      </div>
                      <p className="text-3xl font-semibold text-slate-900 dark:text-white">
                        {skin.marketPrice}
                      </p>
                    </div>
                  </section>
                  {skin.hasHistory && (
                    <section className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-linear-to-br from-emerald-50 to-teal-50 dark:from-slate-900 dark:to-slate-800 p-4 space-y-4">
                      <div className="space-y-1">
                        <p className="text-lg font-semibold text-blue-700 dark:text-blue-300">
                          Projeção de ganho líquido
                        </p>
                        <p className="text-xs text-emerald-900/80 dark:text-emerald-100/80">
                          Comparativo direto entre Steam e{" "}
                          <span className="capitalize">
                            {skin.marketSourceDisplay}
                          </span>
                        </p>
                      </div>
                      <Tabs defaultValue="average" className="w-full">
                        <TabsList className="grid grid-cols-3 bg-white/60 dark:bg-slate-900/40">
                          <TabsTrigger value="average" className="text-xs">
                            Média
                          </TabsTrigger>
                          {skin.expectedGainVsLastSale !== "N/A" && (
                            <TabsTrigger value="last-sale" className="text-xs">
                              Última venda
                            </TabsTrigger>
                          )}
                          {skin.expectedGainVsLowestBuyOrder !== "N/A" && (
                            <TabsTrigger
                              value="lowest-order"
                              className="text-xs"
                            >
                              Menor ordem
                            </TabsTrigger>
                          )}
                        </TabsList>
                        <TabsContent value="average" className="mt-1">
                          <div className="rounded-xl bg-white/85 dark:bg-slate-900/30 border border-emerald-100/60 dark:border-slate-800 p-4 space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
                                Steam · média 10 vendas
                              </span>
                              <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                {skin.steamAveragePrice}
                              </span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-xs font-medium text-slate-600 dark:text-slate-300 capitalize">
                                {skin.marketSourceDisplay}
                              </span>
                              <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                {skin.marketPrice}
                              </span>
                            </div>
                            <div className="flex items-center justify-between border-t border-emerald-100/70 dark:border-slate-800 pt-3">
                              <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                                Ganho estimado
                              </span>
                              <span
                                className={`text-lg font-semibold ${skin.expectedGainColorClass}`}
                              >
                                {skin.expectedGainUsd}
                              </span>
                            </div>
                          </div>
                        </TabsContent>
                        {skin.expectedGainVsLastSale !== "N/A" && (
                          <TabsContent value="last-sale" className="mt-1">
                            <div className="rounded-xl bg-white/85 dark:bg-slate-900/30 border border-emerald-100/60 dark:border-slate-800 p-4 space-y-3">
                              <div className="flex items-center justify-between">
                                <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
                                  Steam · última venda
                                </span>
                                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                  {skin.lastSalePrice}
                                </span>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-xs font-medium text-slate-600 dark:text-slate-300 capitalize">
                                  {skin.marketSourceDisplay}
                                </span>
                                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                  {skin.marketPrice}
                                </span>
                              </div>
                              <div className="flex items-center justify-between border-t border-emerald-100/70 dark:border-slate-800 pt-3">
                                <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                                  Ganho estimado
                                </span>
                                <span
                                  className={`text-lg font-semibold ${skin.expectedGainVsLastSaleColorClass}`}
                                >
                                  {skin.expectedGainVsLastSale}
                                </span>
                              </div>
                            </div>
                          </TabsContent>
                        )}
                        {skin.expectedGainVsLowestBuyOrder !== "N/A" && (
                          <TabsContent value="lowest-order" className="mt-1">
                            <div className="rounded-xl bg-white/85 dark:bg-slate-900/30 border border-emerald-100/60 dark:border-slate-800 p-4 space-y-3">
                              <div className="flex items-center justify-between">
                                <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
                                  Steam · menor ordem ativa
                                </span>
                                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                  {skin.lowestBuyOrderPrice}
                                </span>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-xs font-medium text-slate-600 dark:text-slate-300 capitalize">
                                  {skin.marketSourceDisplay}
                                </span>
                                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                                  {skin.marketPrice}
                                </span>
                              </div>
                              <div className="flex items-center justify-between border-t border-emerald-100/70 dark:border-slate-800 pt-3">
                                <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                                  Ganho estimado
                                </span>
                                <span
                                  className={`text-lg font-semibold ${skin.expectedGainVsLowestBuyOrderColorClass}`}
                                >
                                  {skin.expectedGainVsLowestBuyOrder}
                                </span>
                              </div>
                            </div>
                          </TabsContent>
                        )}
                      </Tabs>
                    </section>
                  )}
                </CardContent>

                <CardFooter className="-mt-6 border-t border-slate-100 dark:border-slate-800 pt-4 flex-col gap-3">
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
                    {/* <Button
                      onClick={() => onIndicatorsOpen(skin.skinId)}
                      variant="ghost"
                      size="sm"
                      asChild
                      className="flex-1 gap-2 border-slate-200/70 bg-white/80 hover:bg-white"
                    >
                      
                    </Button> */}
                  </div>

                  {/* Last Updated */}
                  <p className="text-xs font-medium text-slate-600 dark:text-slate-300 w-full text-center">
                    Atualizado {skin.lastUpdatedRelative}
                  </p>
                  <Button
                    asChild
                    variant="outline"
                    onClick={() => onIndicatorsOpen(skin.skinId)}
                    className="cursor-pointer w-full flex-1 gap-2 border-slate-200/70 bg-white/80 hover:bg-white"
                  >
                    <span>
                      <ChartArea size={14} />
                      Ver indicadores rápidos
                    </span>
                  </Button>
                  <Dialog
                    open={activeIndicatorsSkinId === skin.skinId}
                    onOpenChange={(isOpen) =>
                      isOpen
                        ? onIndicatorsOpen(skin.skinId)
                        : onIndicatorsClose()
                    }
                  >
                    <DialogContent className="max-w-2xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 rounded-3xl shadow-2xl">
                      <DialogHeader className="space-y-1">
                        <DialogTitle className="text-lg font-semibold text-slate-900 dark:text-white">
                          Indicadores rápidos
                        </DialogTitle>
                        <DialogDescription className="text-sm text-slate-500 dark:text-slate-400">
                          Margens calculadas entre Steam e{" "}
                          <span className="capitalize">
                            {skin.marketSourceDisplay}
                          </span>
                        </DialogDescription>
                      </DialogHeader>
                      <div className="grid gap-3 sm:grid-cols-2">
                        <div className="rounded-2xl border border-emerald-100/70 dark:border-emerald-900/40 bg-white/95 dark:bg-slate-900/40 p-3 space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] font-semibold uppercase tracking-[0.2em] text-emerald-700 dark:text-emerald-300">
                              Margem média Steam
                            </span>
                            <Badge
                              variant={skin.profitBadgeVariant}
                              className="text-[11px] font-semibold tracking-tight px-3 py-1"
                            >
                              {skin.profitPercentage}
                            </Badge>
                          </div>
                          <div className="h-1.5 rounded-full bg-linear-to-r from-emerald-400/60 via-emerald-300/40 to-emerald-200/30"></div>
                          <p className="text-[11px] text-slate-500 dark:text-slate-400">
                            Referência na média recente da Steam
                          </p>
                        </div>
                        {skin.hasHistory &&
                          skin.profitPercentageVsLastSale !== "N/A" && (
                            <div className="rounded-2xl border border-amber-100/60 dark:border-amber-900/40 bg-white/95 dark:bg-slate-900/40 p-3 space-y-2">
                              <div className="flex items-center justify-between">
                                <span className="text-[11px] font-semibold uppercase tracking-[0.2em] text-amber-700 dark:text-amber-300">
                                  Margem última venda
                                </span>
                                <Badge
                                  variant={skin.profitVsLastSaleBadgeVariant}
                                  className="text-[11px] font-semibold tracking-tight px-3 py-1"
                                >
                                  {skin.profitPercentageVsLastSale}
                                </Badge>
                              </div>
                              <div className="h-1.5 rounded-full bg-linear-to-r from-amber-400/60 via-amber-300/40 to-amber-200/30"></div>
                              <p className="text-[11px] text-slate-500 dark:text-slate-400">
                                Comparativo com a última venda oficial
                              </p>
                            </div>
                          )}
                        {skin.hasHistory &&
                          skin.profitPercentageVsLowestBuyOrder !== "N/A" && (
                            <div className="rounded-2xl border border-indigo-100/60 dark:border-indigo-900/40 bg-white/95 dark:bg-slate-900/40 p-3 space-y-2">
                              <div className="flex items-center justify-between">
                                <span className="text-[11px] font-semibold uppercase tracking-[0.2em] text-indigo-700 dark:text-indigo-300">
                                  Margem menor ordem
                                </span>
                                <Badge
                                  variant={
                                    skin.profitVsLowestBuyOrderBadgeVariant
                                  }
                                  className="text-[11px] font-semibold tracking-tight px-3 py-1"
                                >
                                  {skin.profitPercentageVsLowestBuyOrder}
                                </Badge>
                              </div>
                              <div className="h-1.5 rounded-full bg-linear-to-r from-indigo-400/60 via-indigo-300/40 to-indigo-200/30"></div>
                              <p className="text-[11px] text-slate-500 dark:text-slate-400">
                                Baseada na menor ordem ativa da Steam
                              </p>
                            </div>
                          )}
                        <div className="rounded-2xl border border-sky-100/70 dark:border-sky-900/40 bg-white/95 dark:bg-slate-900/40 p-3 space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] font-semibold uppercase tracking-[0.2em] text-sky-700 dark:text-sky-300">
                              Desconto marketplace
                            </span>
                            <Badge
                              variant={skin.discountBadgeVariant}
                              className="text-[11px] font-semibold tracking-tight px-3 py-1"
                            >
                              {skin.discountPercentage}
                            </Badge>
                          </div>
                          <div className="h-1.5 rounded-full bg-linear-to-r from-sky-400/60 via-sky-300/40 to-sky-200/30"></div>
                          <p className="text-[11px] text-slate-500 dark:text-slate-400">
                            Gap atual entre marketplace e Steam
                          </p>
                        </div>
                      </div>
                    </DialogContent>
                  </Dialog>
                </CardFooter>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
