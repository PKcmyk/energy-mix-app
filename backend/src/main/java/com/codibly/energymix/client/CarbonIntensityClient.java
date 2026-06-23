package com.codibly.energymix.client;

import com.codibly.energymix.client.dto.GenerationResponse;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CarbonIntensityClient {

    private final RestClient restClient;

    @Value("${app.carbon-intensity.base-url}")
    private final String baseUrl;

    @Cacheable(cacheNames = "generation", unless = "#result == null")
    public GenerationResponse getGeneration(Instant from, Instant to) {
        String uri =
                baseUrl
                        + "/generation/"
                        + DateTimeFormatter.ISO_INSTANT.format(from)
                        + "/"
                        + DateTimeFormatter.ISO_INSTANT.format(to);
        return restClient.get().uri(URI.create(uri)).retrieve().body(GenerationResponse.class);
    }
}
