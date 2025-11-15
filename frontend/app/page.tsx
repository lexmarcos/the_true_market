"use client";

import { useState } from "react";
import { useProfitableSkins } from "./models/profitable-skins.model";
import { HomeView } from "./views/home.view";

/**
 * Home Page ViewModel
 * Orchestrates Model and View - NO business logic here
 */
export default function Home() {
  // State for sorting parameters
  const [sortBy, setSortBy] = useState<"profit" | "discount" | "gain">(
    "profit"
  );
  const [order, setOrder] = useState<"asc" | "desc">("desc");
  const [activeIndicatorsSkinId, setActiveIndicatorsSkinId] = useState<
    string | null
  >(null);

  // Instantiate the Model hook
  const { skins, isLoading, error, refetch, totalCount, hasProfitableSkins } =
    useProfitableSkins({
      sortBy,
      order,
    });

  // Get site name from environment
  const siteName = process.env.NEXT_PUBLIC_SITE_NAME || "The True Market";

  // Pass all props to View
  return (
    <HomeView
      skins={skins}
      isLoading={isLoading}
      error={error}
      refetch={refetch}
      totalCount={totalCount}
      hasProfitableSkins={hasProfitableSkins}
      siteName={siteName}
      sortBy={sortBy}
      order={order}
      onSortByChange={setSortBy}
      onOrderChange={setOrder}
      activeIndicatorsSkinId={activeIndicatorsSkinId}
      onIndicatorsOpen={setActiveIndicatorsSkinId}
      onIndicatorsClose={() => setActiveIndicatorsSkinId(null)}
    />
  );
}
