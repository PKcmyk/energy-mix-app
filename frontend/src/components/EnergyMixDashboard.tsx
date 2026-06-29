import { useEffect, useState } from 'react';
import type { DailyMix } from '../api/types';
import { fetchEnergyMix } from '../api/client';
import { DailyMixCard } from './DailyMixCard';

type State =
  | { status: 'loading' }
  | { status: 'waking' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: DailyMix[] };

export function EnergyMixDashboard() {
  const [state, setState] = useState<State>({ status: 'loading' });

  useEffect(() => {
    const controller = new AbortController();
    fetchEnergyMix({
      signal: controller.signal,
      onWake: () => setState((prev) => (prev.status === 'loading' ? { status: 'waking' } : prev)),
    })
      .then((data) => setState({ status: 'ready', data }))
      .catch((error: Error) => {
        if (error.name === 'AbortError') return;
        setState({ status: 'error', message: error.message });
      });
    return () => controller.abort();
  }, []);

  if (state.status === 'loading') {
    return <p className="status">Loading energy mix…</p>;
  }
  if (state.status === 'waking') {
    return (
      <p className="status">
        Waking the backend up — free hosting sleeps when idle, this can take up to a minute…
      </p>
    );
  }
  if (state.status === 'error') {
    return <p className="status status--error">Could not load energy mix: {state.message}</p>;
  }

  return (
    <div className="cards">
      {state.data.map((mix) => (
        <DailyMixCard key={mix.date} mix={mix} />
      ))}
    </div>
  );
}
