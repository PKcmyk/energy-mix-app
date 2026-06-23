package com.codibly.energymix.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.codibly.energymix.client.dto.GenerationResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CarbonIntensityClientTest {

    private static final String BASE_URL = "https://api.example.test";

    private MockRestServiceServer server;
    private CarbonIntensityClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CarbonIntensityClient(builder.build(), BASE_URL);
    }

    @Test
    void buildsUnencodedUriAndDeserialisesResponse() {
        // given
        String body =
                """
                {
                  "data": [
                    {
                      "from": "2026-06-23T00:00Z",
                      "to": "2026-06-23T00:30Z",
                      "generationmix": [
                        {"fuel": "wind", "perc": 23.1},
                        {"fuel": "gas", "perc": 45.7}
                      ]
                    }
                  ]
                }
                """;
        server.expect(requestTo(BASE_URL + "/generation/2026-06-23T00:00:00Z/2026-06-23T03:00:00Z"))
                .andExpect(method(GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        // when
        GenerationResponse response =
                client.getGeneration(
                        Instant.parse("2026-06-23T00:00:00Z"),
                        Instant.parse("2026-06-23T03:00:00Z"));

        // then
        server.verify();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).generationmix())
                .extracting("fuel")
                .containsExactly("wind", "gas");
        assertThat(response.data().get(0).generationmix().get(0).perc()).isEqualTo(23.1);
    }
}
