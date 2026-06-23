import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DailyMixCard } from '../DailyMixCard';

describe('DailyMixCard', () => {
  it('shows the clean-energy share and a legend entry per fuel', () => {
    render(
      <DailyMixCard
        mix={{
          date: '2026-06-23',
          generationMix: { wind: 20, solar: 25, gas: 55 },
          cleanEnergyPercentage: 45,
        }}
      />,
    );

    expect(screen.getByText('45.0%')).toBeInTheDocument();
    expect(screen.getByText(/Wind/)).toBeInTheDocument();
    expect(screen.getByText(/Solar/)).toBeInTheDocument();
    expect(screen.getByText('55.0%')).toBeInTheDocument();
    expect(screen.getByText('20.0%')).toBeInTheDocument();
  });

  it('omits fuels with a zero share', () => {
    render(
      <DailyMixCard
        mix={{
          date: '2026-06-24',
          generationMix: { wind: 100, coal: 0 },
          cleanEnergyPercentage: 100,
        }}
      />,
    );

    expect(screen.queryByText(/Coal/)).not.toBeInTheDocument();
  });
});
