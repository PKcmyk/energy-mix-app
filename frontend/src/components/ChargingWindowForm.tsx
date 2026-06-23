import { useState, type FormEvent } from 'react';
import type { ChargingWindow } from '../api/types';
import { fetchChargingWindow } from '../api/client';
import { ChargingWindowResult } from './ChargingWindowResult';

const MIN_HOURS = 1;
const MAX_HOURS = 6;

export function ChargingWindowForm() {
  const [hours, setHours] = useState(3);
  const [result, setResult] = useState<ChargingWindow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const outOfRange = hours < MIN_HOURS || hours > MAX_HOURS || !Number.isInteger(hours);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (outOfRange) {
      setError(`Please choose between ${MIN_HOURS} and ${MAX_HOURS} hours.`);
      return;
    }
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      setResult(await fetchChargingWindow(hours));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="planner">
      <form className="planner__form" onSubmit={handleSubmit}>
        <label htmlFor="hours" className="planner__label">
          How many hours do you need to charge?
        </label>
        <div className="planner__controls">
          <input
            id="hours"
            type="number"
            min={MIN_HOURS}
            max={MAX_HOURS}
            step={1}
            value={Number.isNaN(hours) ? '' : hours}
            onChange={(event) => setHours(event.target.valueAsNumber)}
            aria-describedby="hours-help"
          />
          <button type="submit" disabled={loading || outOfRange}>
            {loading ? 'Calculating…' : 'Find greenest window'}
          </button>
        </div>
        <p id="hours-help" className="planner__help">
          Choose a whole number of hours between {MIN_HOURS} and {MAX_HOURS}.
        </p>
      </form>

      {error && (
        <p className="status status--error" role="alert">
          {error}
        </p>
      )}
      {result && <ChargingWindowResult window={result} />}
    </section>
  );
}
