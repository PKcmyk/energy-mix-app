import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ChargingWindowForm } from '../ChargingWindowForm';
import { fetchChargingWindow } from '../../api/client';

vi.mock('../../api/client', () => ({
  fetchChargingWindow: vi.fn(),
}));

const mockedFetch = vi.mocked(fetchChargingWindow);

describe('ChargingWindowForm', () => {
  afterEach(() => vi.clearAllMocks());

  it('submits the chosen number of hours and renders the result', async () => {
    mockedFetch.mockResolvedValue({
      start: '2026-06-24T11:00:00Z',
      end: '2026-06-24T14:00:00Z',
      averageCleanEnergyPercentage: 83.75,
    });

    render(<ChargingWindowForm />);
    await userEvent.click(screen.getByRole('button', { name: /find greenest window/i }));

    expect(mockedFetch).toHaveBeenCalledWith(3, expect.objectContaining({ onWake: expect.any(Function) }));
    expect(await screen.findByText(/greenest time to charge/i)).toBeInTheDocument();
    expect(screen.getByText('83.8%')).toBeInTheDocument();
  });

  it('shows an error message when the request fails', async () => {
    mockedFetch.mockRejectedValue(new Error('Service unavailable'));

    render(<ChargingWindowForm />);
    await userEvent.click(screen.getByRole('button', { name: /find greenest window/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Service unavailable');
  });

  it('disables the submit button for an out-of-range value', async () => {
    render(<ChargingWindowForm />);
    const input = screen.getByLabelText(/how many hours/i);

    await userEvent.clear(input);
    await userEvent.type(input, '9');

    expect(screen.getByRole('button', { name: /find greenest window/i })).toBeDisabled();
    expect(mockedFetch).not.toHaveBeenCalled();
  });
});
