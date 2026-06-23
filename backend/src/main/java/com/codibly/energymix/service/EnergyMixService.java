package com.codibly.energymix.service;

import com.codibly.energymix.client.CarbonIntensityClient;
import com.codibly.energymix.client.dto.FuelShare;
import com.codibly.energymix.client.dto.GenerationPeriod;
import com.codibly.energymix.client.dto.GenerationResponse;
import com.codibly.energymix.config.AppConfig;
import com.codibly.energymix.dto.ChargingWindowDto;
import com.codibly.energymix.dto.DailyMixDto;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnergyMixService {

    static final Set<String> CLEAN_FUELS = Set.of("biomass", "nuclear", "hydro", "wind", "solar");

    private static final int REPORTED_DAYS = 3;

    private static final int INTERVALS_PER_HOUR = 2;

    private final CarbonIntensityClient client;
    private final Clock clock;

    public List<DailyMixDto> getThreeDayMix() {
        LocalDate today = LocalDate.now(clock);
        Instant from = today.atStartOfDay(AppConfig.UK_ZONE).toInstant();
        Instant to = today.plusDays(REPORTED_DAYS).atStartOfDay(AppConfig.UK_ZONE).toInstant();

        Map<LocalDate, List<GenerationPeriod>> byDate =
                fetch(from, to).stream()
                        .collect(
                                Collectors.groupingBy(
                                        this::dateOf, TreeMap::new, Collectors.toList()));

        List<DailyMixDto> result = new ArrayList<>(REPORTED_DAYS);
        for (int dayOffset = 0; dayOffset < REPORTED_DAYS; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            result.add(aggregateDay(date, byDate.getOrDefault(date, List.of())));
        }
        return result;
    }

    public ChargingWindowDto getOptimalChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("hours must be between 1 and 6, was " + hours);
        }
        int windowSize = hours * INTERVALS_PER_HOUR;

        LocalDate today = LocalDate.now(clock);
        Instant from = today.plusDays(1).atStartOfDay(AppConfig.UK_ZONE).toInstant();
        Instant to = today.plusDays(REPORTED_DAYS).atStartOfDay(AppConfig.UK_ZONE).toInstant();

        List<GenerationPeriod> periods =
                fetch(from, to).stream()
                        .sorted(Comparator.comparing(GenerationPeriod::from))
                        .toList();

        double bestAverage = Double.NEGATIVE_INFINITY;
        int bestStart = -1;
        for (int start = 0; start + windowSize <= periods.size(); start++) {
            if (!isContiguous(periods, start, windowSize)) {
                continue;
            }
            double average = averageCleanShare(periods, start, windowSize);
            if (average > bestAverage) {
                bestAverage = average;
                bestStart = start;
            }
        }

        if (bestStart < 0) {
            throw new InsufficientForecastDataException(
                    "Not enough contiguous forecast data for a " + hours + "h window");
        }

        GenerationPeriod windowStart = periods.get(bestStart);
        GenerationPeriod windowEnd = periods.get(bestStart + windowSize - 1);
        return new ChargingWindowDto(windowStart.from(), windowEnd.to(), round(bestAverage));
    }

    private DailyMixDto aggregateDay(LocalDate date, List<GenerationPeriod> periods) {
        if (periods.isEmpty()) {
            return new DailyMixDto(date, Map.of(), 0.0);
        }

        Map<String, Double> totals = new LinkedHashMap<>();
        for (GenerationPeriod period : periods) {
            for (FuelShare share : period.generationmix()) {
                totals.merge(share.fuel(), share.perc(), Double::sum);
            }
        }

        int periodCount = periods.size();
        Map<String, Double> average = new LinkedHashMap<>();
        totals.forEach((fuel, sum) -> average.put(fuel, round(sum / periodCount)));

        double cleanSum =
                CLEAN_FUELS.stream().mapToDouble(fuel -> totals.getOrDefault(fuel, 0.0)).sum();
        return new DailyMixDto(date, average, round(cleanSum / periodCount));
    }

    private double averageCleanShare(List<GenerationPeriod> periods, int start, int size) {
        double sum = 0;
        for (int i = start; i < start + size; i++) {
            sum += cleanShareOf(periods.get(i));
        }
        return sum / size;
    }

    private double cleanShareOf(GenerationPeriod period) {
        return period.generationmix().stream()
                .filter(share -> CLEAN_FUELS.contains(share.fuel()))
                .mapToDouble(FuelShare::perc)
                .sum();
    }

    private boolean isContiguous(List<GenerationPeriod> periods, int start, int size) {
        for (int i = start; i < start + size - 1; i++) {
            if (!periods.get(i).to().isEqual(periods.get(i + 1).from())) {
                return false;
            }
        }
        return true;
    }

    private LocalDate dateOf(GenerationPeriod period) {
        return period.from().atZoneSameInstant(AppConfig.UK_ZONE).toLocalDate();
    }

    private List<GenerationPeriod> fetch(Instant from, Instant to) {
        GenerationResponse response = client.getGeneration(from, to);
        return response == null || response.data() == null ? List.of() : response.data();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
