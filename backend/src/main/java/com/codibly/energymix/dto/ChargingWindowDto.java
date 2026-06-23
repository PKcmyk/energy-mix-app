package com.codibly.energymix.dto;

import java.time.OffsetDateTime;

/**
 * Optimal EV charging window: the contiguous period with the highest average clean-energy share.
 *
 * @param start                         start timestamp (inclusive)
 * @param end                           end timestamp (exclusive)
 * @param averageCleanEnergyPercentage  average clean-energy share [%] across the window
 */
public record ChargingWindowDto(OffsetDateTime start, OffsetDateTime end, double averageCleanEnergyPercentage) {
}
