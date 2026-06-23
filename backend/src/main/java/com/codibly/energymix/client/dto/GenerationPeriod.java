package com.codibly.energymix.client.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record GenerationPeriod(
        OffsetDateTime from, OffsetDateTime to, List<FuelShare> generationmix) {}
