import type { ChargingWindow, DailyMix } from './types';

const API_BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');

// On Render's free tier the backend spins down after ~15 min idle and a cold
// start takes ~45-75s, during which the edge returns 502/503/504. Rather than
// surfacing that as a hard error, retry until the backend wakes up.
const RETRYABLE_STATUS = new Set([502, 503, 504]);
const MAX_TOTAL_WAIT_MS = 90_000;
const INITIAL_BACKOFF_MS = 1_500;
const MAX_BACKOFF_MS = 6_000;

export interface RequestOptions {
  /** Cancel the request (and any pending retries), e.g. on component unmount. */
  signal?: AbortSignal;
  /** Called before each retry so the UI can show a "waking the backend" hint. */
  onWake?: (attempt: number) => void;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const url = `${API_BASE_URL}${path}`;
  const deadline = now() + MAX_TOTAL_WAIT_MS;
  let backoff = INITIAL_BACKOFF_MS;
  let attempt = 0;

  while (true) {
    throwIfAborted(options.signal);
    attempt += 1;

    let response: Response | undefined;
    try {
      response = await fetch(url);
    } catch (networkError) {
      // Backend unreachable (connection refused while it boots) — retry.
      if (!canRetry(options.signal, deadline)) {
        throw networkError;
      }
    }

    if (response) {
      if (response.ok) {
        return (await response.json()) as T;
      }
      // 4xx are real errors (e.g. invalid hours) — never retry those.
      if (!RETRYABLE_STATUS.has(response.status) || !canRetry(options.signal, deadline)) {
        throw new Error(await errorMessage(response));
      }
    }

    options.onWake?.(attempt);
    await delay(backoff, options.signal);
    backoff = Math.min(Math.round(backoff * 1.5), MAX_BACKOFF_MS);
  }
}

function canRetry(signal: AbortSignal | undefined, deadline: number): boolean {
  return !signal?.aborted && now() < deadline;
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

function delay(ms: number, signal?: AbortSignal): Promise<void> {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) {
      reject(abortError());
      return;
    }
    const id = setTimeout(resolve, ms);
    signal?.addEventListener(
      'abort',
      () => {
        clearTimeout(id);
        reject(abortError());
      },
      { once: true },
    );
  });
}

function throwIfAborted(signal?: AbortSignal): void {
  if (signal?.aborted) {
    throw abortError();
  }
}

function abortError(): DOMException {
  return new DOMException('The request was aborted.', 'AbortError');
}

function now(): number {
  return Date.now();
}

export function fetchEnergyMix(options?: RequestOptions): Promise<DailyMix[]> {
  return request<DailyMix[]>('/api/energy-mix', options);
}

export function fetchChargingWindow(hours: number, options?: RequestOptions): Promise<ChargingWindow> {
  return request<ChargingWindow>(`/api/charging-window?hours=${hours}`, options);
}
