package com.codibly.energymix.controller;

import com.codibly.energymix.dto.ChargingWindowDto;
import com.codibly.energymix.dto.DailyMixDto;
import com.codibly.energymix.service.EnergyMixService;
import com.codibly.energymix.service.InsufficientForecastDataException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnergyMixController.class)
class EnergyMixControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyMixService service;

    @Test
    void returnsThreeDayMix() throws Exception {
        when(service.getThreeDayMix()).thenReturn(List.of(
                new DailyMixDto(LocalDate.parse("2026-06-23"), Map.of("wind", 30.0, "gas", 70.0), 30.0)
        ));

        mockMvc.perform(get("/api/energy-mix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-23"))
                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value(30.0))
                .andExpect(jsonPath("$[0].generationMix.wind").value(30.0));
    }

    @Test
    void returnsChargingWindow() throws Exception {
        when(service.getOptimalChargingWindow(3)).thenReturn(new ChargingWindowDto(
                OffsetDateTime.parse("2026-06-24T11:00:00Z"),
                OffsetDateTime.parse("2026-06-24T14:00:00Z"),
                83.75));

        mockMvc.perform(get("/api/charging-window").param("hours", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start").value("2026-06-24T11:00:00Z"))
                .andExpect(jsonPath("$.end").value("2026-06-24T14:00:00Z"))
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(83.75));
    }

    @Test
    void rejectsHoursAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/charging-window").param("hours", "7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingHours() throws Exception {
        mockMvc.perform(get("/api/charging-window"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mapsInsufficientForecastToUnprocessableEntity() throws Exception {
        when(service.getOptimalChargingWindow(anyInt()))
                .thenThrow(new InsufficientForecastDataException("no data"));

        mockMvc.perform(get("/api/charging-window").param("hours", "6"))
                .andExpect(status().isUnprocessableEntity());
    }
}
