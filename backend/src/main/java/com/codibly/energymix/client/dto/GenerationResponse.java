package com.codibly.energymix.client.dto;

import java.util.List;

/**
 * Root payload of the {@code GET /generation/{from}/{to}} Carbon Intensity endpoint.
 */
public record GenerationResponse(List<GenerationPeriod> data) {
}
