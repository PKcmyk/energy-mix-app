package com.codibly.energymix.dto;

import java.time.OffsetDateTime;

public record ChargingWindowDto(
        OffsetDateTime start, OffsetDateTime end, double averageCleanEnergyPercentage) {}
