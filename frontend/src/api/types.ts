export interface DailyMix {
  date: string;
  generationMix: Record<string, number>;
  cleanEnergyPercentage: number;
}

export interface ChargingWindow {
  start: string;
  end: string;
  averageCleanEnergyPercentage: number;
}
