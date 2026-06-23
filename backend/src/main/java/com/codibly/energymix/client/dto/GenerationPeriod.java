package com.codibly.energymix.client.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A single half-hour settlement period and its generation mix.
 */
public record GenerationPeriod(OffsetDateTime from, OffsetDateTime to, List<FuelShare> generationmix) {
}
