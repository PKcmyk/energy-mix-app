package com.codibly.energymix.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codibly.energymix.client.CarbonIntensityClient;
import com.codibly.energymix.client.dto.FuelShare;
import com.codibly.energymix.client.dto.GenerationPeriod;
import com.codibly.energymix.client.dto.GenerationResponse;
import com.codibly.energymix.dto.ChargingWindowDto;
import com.codibly.energymix.dto.DailyMixDto;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnergyMixServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-06-23T10:00:00Z"), ZoneId.of("Europe/London"));

    @Mock private CarbonIntensityClient client;

    private EnergyMixService service() {
        return new EnergyMixService(client, CLOCK);
    }

    @Test
    void groupsPeriodsByDayAndAveragesEachFuel() {
        // given
        List<GenerationPeriod> periods = new ArrayList<>();
        periods.add(period("2026-06-23T10:00:00Z", share("wind", 20), share("gas", 80)));
        periods.add(period("2026-06-23T10:30:00Z", share("wind", 40), share("gas", 60)));
        periods.add(period("2026-06-24T10:00:00Z", share("solar", 50), share("gas", 50)));
        periods.add(period("2026-06-24T10:30:00Z", share("solar", 10), share("gas", 90)));
        periods.add(period("2026-06-25T10:00:00Z", share("nuclear", 100), share("gas", 0)));
        periods.add(period("2026-06-25T10:30:00Z", share("nuclear", 50), share("gas", 50)));
        when(client.getGeneration(any(), any())).thenReturn(new GenerationResponse(periods));

        // when
        List<DailyMixDto> result = service().getThreeDayMix();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).date()).hasToString("2026-06-23");
        assertThat(result.get(0).generationMix())
                .containsEntry("wind", 30.0)
                .containsEntry("gas", 70.0);
        assertThat(result.get(0).cleanEnergyPercentage()).isEqualTo(30.0);
        assertThat(result.get(1).cleanEnergyPercentage()).isEqualTo(30.0);
        assertThat(result.get(2).generationMix()).containsEntry("nuclear", 75.0);
        assertThat(result.get(2).cleanEnergyPercentage()).isEqualTo(75.0);
    }

    @Test
    void returnsEmptyDayWhenApiHasNoDataForIt() {
        // given
        when(client.getGeneration(any(), any()))
                .thenReturn(
                        new GenerationResponse(
                                List.of(
                                        period(
                                                "2026-06-23T10:00:00Z",
                                                share("wind", 50),
                                                share("gas", 50)))));

        // when
        List<DailyMixDto> result = service().getThreeDayMix();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(1).generationMix()).isEmpty();
        assertThat(result.get(1).cleanEnergyPercentage()).isZero();
    }

    @Test
    void picksWindowWithHighestCleanShare() {
        // given
        List<GenerationPeriod> periods =
                List.of(
                        period("2026-06-24T10:00:00Z", share("wind", 10)),
                        period("2026-06-24T10:30:00Z", share("wind", 20)),
                        period("2026-06-24T11:00:00Z", share("wind", 90)),
                        period("2026-06-24T11:30:00Z", share("wind", 95)),
                        period("2026-06-24T12:00:00Z", share("wind", 80)),
                        period("2026-06-24T12:30:00Z", share("wind", 70)));
        when(client.getGeneration(any(), any())).thenReturn(new GenerationResponse(periods));

        // when
        ChargingWindowDto window = service().getOptimalChargingWindow(2);

        // then
        assertThat(window.start()).isEqualTo(OffsetDateTime.parse("2026-06-24T11:00:00Z"));
        assertThat(window.end()).isEqualTo(OffsetDateTime.parse("2026-06-24T13:00:00Z"));
        assertThat(window.averageCleanEnergyPercentage()).isEqualTo(83.75);
    }

    @Test
    void chargingWindowMaySpanMidnight() {
        // given
        List<GenerationPeriod> periods =
                List.of(
                        period("2026-06-24T22:00:00Z", share("wind", 10)),
                        period("2026-06-24T22:30:00Z", share("wind", 10)),
                        period("2026-06-24T23:00:00Z", share("wind", 90)),
                        period("2026-06-24T23:30:00Z", share("wind", 95)),
                        period("2026-06-25T00:00:00Z", share("wind", 92)),
                        period("2026-06-25T00:30:00Z", share("wind", 88)));
        when(client.getGeneration(any(), any())).thenReturn(new GenerationResponse(periods));

        // when
        ChargingWindowDto window = service().getOptimalChargingWindow(2);

        // then
        assertThat(window.start()).isEqualTo(OffsetDateTime.parse("2026-06-24T23:00:00Z"));
        assertThat(window.end()).isEqualTo(OffsetDateTime.parse("2026-06-25T01:00:00Z"));
        assertThat(window.averageCleanEnergyPercentage()).isEqualTo(91.25);
    }

    @Test
    void skipsWindowsThatAreNotContiguous() {
        // given
        List<GenerationPeriod> periods =
                List.of(
                        period("2026-06-24T10:00:00Z", share("wind", 10)),
                        period("2026-06-24T10:30:00Z", share("wind", 10)),
                        period("2026-06-24T13:00:00Z", share("wind", 99)),
                        period("2026-06-24T13:30:00Z", share("wind", 99)));
        when(client.getGeneration(any(), any())).thenReturn(new GenerationResponse(periods));

        // when
        ChargingWindowDto window = service().getOptimalChargingWindow(1);

        // then
        assertThat(window.start()).isEqualTo(OffsetDateTime.parse("2026-06-24T13:00:00Z"));
        assertThat(window.averageCleanEnergyPercentage()).isEqualTo(99.0);
    }

    @Test
    void throwsWhenNotEnoughForecastData() {
        // given
        when(client.getGeneration(any(), any()))
                .thenReturn(
                        new GenerationResponse(
                                List.of(period("2026-06-24T10:00:00Z", share("wind", 50)))));

        // when / then
        assertThatThrownBy(() -> service().getOptimalChargingWindow(3))
                .isInstanceOf(InsufficientForecastDataException.class);
    }

    @Test
    void rejectsHoursOutsideAllowedRange() {
        // when / then
        assertThatThrownBy(() -> service().getOptimalChargingWindow(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service().getOptimalChargingWindow(7))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static GenerationPeriod period(String fromIso, FuelShare... shares) {
        OffsetDateTime from = OffsetDateTime.parse(fromIso).withOffsetSameInstant(ZoneOffset.UTC);
        return new GenerationPeriod(from, from.plusMinutes(30), List.of(shares));
    }

    private static FuelShare share(String fuel, double perc) {
        return new FuelShare(fuel, perc);
    }
}
