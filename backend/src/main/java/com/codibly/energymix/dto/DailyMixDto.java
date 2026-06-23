package com.codibly.energymix.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Aggregated generation mix for a single day.
 *
 * @param date                   the day the figures apply to
 * @param generationMix          average share [%] of every fuel over that day's half-hour periods
 * @param cleanEnergyPercentage  average share [%] of clean sources (biomass, nuclear, hydro, wind, solar)
 */
public record DailyMixDto(LocalDate date, Map<String, Double> generationMix, double cleanEnergyPercentage) {
}
