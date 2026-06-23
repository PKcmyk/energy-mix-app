package com.codibly.energymix.dto;

import java.time.LocalDate;
import java.util.Map;

public record DailyMixDto(
        LocalDate date, Map<String, Double> generationMix, double cleanEnergyPercentage) {}
