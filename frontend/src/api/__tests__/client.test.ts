import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchChargingWindow, fetchEnergyMix } from '../client';

function mockFetch(response: Partial<Response> & { json: () => Promise<unknown> }) {
  const fetchMock = vi.fn().mockResolvedValue(response);
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

describe('api client', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('fetches the charging window and parses the JSON body', async () => {
    const payload = {
      start: '2026-06-24T11:00:00Z',
      end: '2026-06-24T14:00:00Z',
      averageCleanEnergyPercentage: 83.75,
    };
    const fetchMock = mockFetch({ ok: true, json: async () => payload });

    const result = await fetchChargingWindow(3);

    expect(result).toEqual(payload);
    expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining('/api/charging-window?hours=3'));
  });

  it('requests the energy mix endpoint', async () => {
    const fetchMock = mockFetch({ ok: true, json: async () => [] });

    await fetchEnergyMix();

    expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining('/api/energy-mix'));
  });

  it('surfaces the backend error message on a failed response', async () => {
    mockFetch({ ok: false, status: 400, json: async () => ({ status: 400, message: 'bad hours' }) });

    await expect(fetchChargingWindow(9)).rejects.toThrow('bad hours');
  });

  it('retries 502 cold-start responses until the backend wakes up', async () => {
    vi.useFakeTimers();
    try {
      const fetchMock = vi
        .fn()
        .mockResolvedValueOnce({ ok: false, status: 502, json: async () => ({}) })
        .mockResolvedValueOnce({ ok: true, json: async () => [] });
      vi.stubGlobal('fetch', fetchMock);
      const onWake = vi.fn();

      const promise = fetchEnergyMix({ onWake });
      await vi.runAllTimersAsync();
      await promise;

      expect(fetchMock).toHaveBeenCalledTimes(2);
      expect(onWake).toHaveBeenCalledTimes(1);
    } finally {
      vi.useRealTimers();
    }
  });
});
