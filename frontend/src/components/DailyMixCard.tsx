import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { DailyMix } from '../api/types';
import { fuelColor, formatFuel, isClean } from './fuel';

function formatDate(isoDate: string): string {
  return new Date(`${isoDate}T00:00:00Z`).toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    timeZone: 'UTC',
  });
}

interface Props {
  mix: DailyMix;
}

export function DailyMixCard({ mix }: Props) {
  const slices = Object.entries(mix.generationMix)
    .map(([fuel, value]) => ({ fuel, value }))
    .filter((slice) => slice.value > 0)
    .sort((a, b) => b.value - a.value);

  return (
    <article className="card">
      <header className="card__header">
        <h3>{formatDate(mix.date)}</h3>
        <p className="card__clean" aria-label="Clean energy share">
          <span className="card__clean-value">{mix.cleanEnergyPercentage.toFixed(1)}%</span>
          <span className="card__clean-label">clean energy</span>
        </p>
      </header>

      <div className="card__chart" role="img" aria-label={`Generation mix for ${formatDate(mix.date)}`}>
        <ResponsiveContainer width="100%" height={220}>
          <PieChart>
            <Pie data={slices} dataKey="value" nameKey="fuel" innerRadius={45} outerRadius={85} paddingAngle={1}>
              {slices.map((slice) => (
                <Cell key={slice.fuel} fill={fuelColor(slice.fuel)} />
              ))}
            </Pie>
            <Tooltip formatter={(value: number, name: string) => [`${value.toFixed(1)}%`, formatFuel(name)]} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <ul className="legend">
        {slices.map((slice) => (
          <li key={slice.fuel} className="legend__item">
            <span className="legend__dot" style={{ backgroundColor: fuelColor(slice.fuel) }} />
            <span className="legend__name">
              {formatFuel(slice.fuel)}
              {isClean(slice.fuel) && <span className="legend__clean" title="Clean source"> ●</span>}
            </span>
            <span className="legend__value">{slice.value.toFixed(1)}%</span>
          </li>
        ))}
      </ul>
    </article>
  );
}
