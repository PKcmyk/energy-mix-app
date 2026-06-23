/** Aggregated generation mix for a single day, as returned by the backend. */
export interface DailyMix {
  date: string; // ISO date, e.g. "2026-06-23"
  generationMix: Record<string, number>; // fuel -> average share [%]
  cleanEnergyPercentage: number;
}

/** Optimal charging window returned by the backend. */
export interface ChargingWindow {
  start: string; // ISO date-time
  end: string; // ISO date-time
  averageCleanEnergyPercentage: number;
}
