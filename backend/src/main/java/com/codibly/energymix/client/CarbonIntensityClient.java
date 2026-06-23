package com.codibly.energymix.client;

import com.codibly.energymix.client.dto.GenerationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Thin wrapper around the public UK Carbon Intensity API.
 *
 * @see <a href="https://carbonintensity.github.io/api-definitions/#get-generation-from-to">API docs</a>
 */
@Component
public class CarbonIntensityClient {

    private final RestClient restClient;
    private final String baseUrl;

    public CarbonIntensityClient(RestClient carbonIntensityRestClient,
                                 @Value("${app.carbon-intensity.base-url}") String baseUrl) {
        this.restClient = carbonIntensityRestClient;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetches the national generation mix in half-hour periods for the {@code [from, to)} range.
     * The URI is assembled manually so the colons in the ISO-8601 timestamps are not
     * percent-encoded, which the API requires.
     */
    public GenerationResponse getGeneration(Instant from, Instant to) {
        String uri = baseUrl + "/generation/"
                + DateTimeFormatter.ISO_INSTANT.format(from) + "/"
                + DateTimeFormatter.ISO_INSTANT.format(to);
        return restClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .body(GenerationResponse.class);
    }
}
