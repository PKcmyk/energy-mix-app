export const CLEAN_FUELS = new Set(['biomass', 'nuclear', 'hydro', 'wind', 'solar']);

export const FUEL_COLORS: Record<string, string> = {
  biomass: '#6a994e',
  nuclear: '#9d4edd',
  hydro: '#0096c7',
  wind: '#48cae4',
  solar: '#ffd60a',
  gas: '#e76f51',
  coal: '#495057',
  imports: '#adb5bd',
  other: '#ced4da',
};

export function fuelColor(fuel: string): string {
  return FUEL_COLORS[fuel] ?? '#868e96';
}

export function formatFuel(fuel: string): string {
  return fuel.charAt(0).toUpperCase() + fuel.slice(1);
}

export function isClean(fuel: string): boolean {
  return CLEAN_FUELS.has(fuel);
}
