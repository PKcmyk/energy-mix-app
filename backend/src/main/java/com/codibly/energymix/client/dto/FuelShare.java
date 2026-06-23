package com.codibly.energymix.client.dto;

/**
 * Single fuel entry of the generation mix as returned by the Carbon Intensity API,
 * e.g. {@code {"fuel": "wind", "perc": 23.1}}.
 */
public record FuelShare(String fuel, double perc) {
}
