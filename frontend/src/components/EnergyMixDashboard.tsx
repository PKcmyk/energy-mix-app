import { useEffect, useState } from 'react';
import type { DailyMix } from '../api/types';
import { fetchEnergyMix } from '../api/client';
import { DailyMixCard } from './DailyMixCard';

type State =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: DailyMix[] };

export function EnergyMixDashboard() {
  const [state, setState] = useState<State>({ status: 'loading' });

  useEffect(() => {
    let active = true;
    fetchEnergyMix()
      .then((data) => active && setState({ status: 'ready', data }))
      .catch((error: Error) => active && setState({ status: 'error', message: error.message }));
    return () => {
      active = false;
    };
  }, []);

  if (state.status === 'loading') {
    return <p className="status">Loading energy mix…</p>;
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
