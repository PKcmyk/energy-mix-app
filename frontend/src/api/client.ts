import type { ChargingWindow, DailyMix } from './types';

const API_BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`);
  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }
  return (await response.json()) as T;
}

async function errorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (body && typeof body.message === 'string') {
      return body.message;
    }
  } catch {
  }
  return `Request failed with status ${response.status}`;
}

export function fetchEnergyMix(): Promise<DailyMix[]> {
  return request<DailyMix[]>('/api/energy-mix');
}

export function fetchChargingWindow(hours: number): Promise<ChargingWindow> {
  return request<ChargingWindow>(`/api/charging-window?hours=${hours}`);
}
