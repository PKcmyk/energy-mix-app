package com.codibly.energymix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** UK is the subject of the data, so all "today / tomorrow" reasoning is done in London time. */
    public static final ZoneId UK_ZONE = ZoneId.of("Europe/London");

    @Bean
    RestClient carbonIntensityRestClient(RestClient.Builder builder,
                                         @Value("${app.carbon-intensity.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    setReadTimeout((int) Duration.ofSeconds(15).toMillis());
                }})
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.system(UK_ZONE);
    }
}
