import { memo } from "react";

interface WearIndicatorProps {
  label: string;
  valueLabel: string;
  normalizedValue: number | null;
}

/**
 * Displays a wear indicator bar with a gradient scale and a floating marker.
 * Accepts fully-prepared props from the ViewModel to comply with MVVM constraints.
 */
export const WearIndicator = memo(function WearIndicator({
  label,
  valueLabel,
  normalizedValue,
}: WearIndicatorProps) {
  const safePosition = normalizedValue ?? 0.5;
  const indicatorPosition = Math.min(Math.max(safePosition, 0), 1) * 100;

  return (
    <div className="w-full space-y-1.5">
      <div className="flex items-center justify-between text-[10px] font-semibold uppercase tracking-[0.35em] text-slate-500 dark:text-slate-400">
        <span>{label}</span>
        <span className="text-xs tracking-normal text-slate-900 dark:text-slate-100">
          {valueLabel}
        </span>
      </div>
      <div className="relative h-2 rounded-full bg-gradient-to-r from-emerald-400 via-amber-300 to-rose-500 shadow-inner">
        <div className="absolute inset-0 rounded-full border border-white/40 dark:border-slate-950/50" />
        <span
          className="pointer-events-none absolute top-1/2 h-3 w-0.5 -translate-y-1/2 rounded-full bg-white shadow-[0_0_6px_rgba(15,23,42,0.45)] dark:bg-slate-900"
          style={{ left: `calc(${indicatorPosition}% - 1px)` }}
          aria-hidden="true"
        />
      </div>
    </div>
  );
});
