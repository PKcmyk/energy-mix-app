import { ChargingWindowForm } from './components/ChargingWindowForm';
import { EnergyMixDashboard } from './components/EnergyMixDashboard';

export function App() {
  return (
    <div className="app">
      <header className="app__header">
        <h1>UK Energy Mix &amp; EV Charging Planner</h1>
        <p>
          Live and forecast electricity generation mix for Great Britain, plus the greenest time to
          charge your electric car over the next two days.
        </p>
      </header>

      <main>
        <section className="section">
          <h2 className="section__title">Energy mix — today, tomorrow &amp; the day after</h2>
          <EnergyMixDashboard />
        </section>

        <section className="section">
          <h2 className="section__title">Plan your charging</h2>
          <ChargingWindowForm />
        </section>
      </main>

      <footer className="app__footer">
        Data: <a href="https://carbonintensity.org.uk/" target="_blank" rel="noreferrer">Carbon Intensity API</a>
        {' '}· Clean energy = biomass, nuclear, hydro, wind, solar.
      </footer>
    </div>
  );
}
