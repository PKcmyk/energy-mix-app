import type { ChargingWindow } from '../api/types';

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/London',
  });
}

interface Props {
  window: ChargingWindow;
}

export function ChargingWindowResult({ window }: Props) {
  return (
    <div className="result" role="status">
      <h3 className="result__title">Greenest time to charge</h3>
      <div className="result__grid">
        <div>
          <span className="result__label">Start</span>
          <span className="result__value">{formatDateTime(window.start)}</span>
        </div>
        <div>
          <span className="result__label">End</span>
          <span className="result__value">{formatDateTime(window.end)}</span>
        </div>
        <div>
          <span className="result__label">Average clean energy</span>
          <span className="result__value result__value--accent">
            {window.averageCleanEnergyPercentage.toFixed(1)}%
          </span>
        </div>
      </div>
      <p className="result__hint">Times shown in UK local time.</p>
    </div>
  );
}
