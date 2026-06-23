package com.codibly.energymix.controller;

import com.codibly.energymix.dto.ChargingWindowDto;
import com.codibly.energymix.dto.DailyMixDto;
import com.codibly.energymix.service.EnergyMixService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnergyMixController {

    private final EnergyMixService service;

    public EnergyMixController(EnergyMixService service) {
        this.service = service;
    }

    /** Aggregated generation mix for today, tomorrow and the day after tomorrow. */
    @GetMapping("/energy-mix")
    public List<DailyMixDto> getEnergyMix() {
        return service.getThreeDayMix();
    }

    /** Greenest charging window of the requested length (1-6 full hours) over the next two days. */
    @GetMapping("/charging-window")
    public ChargingWindowDto getChargingWindow(@RequestParam @Min(1) @Max(6) int hours) {
        return service.getOptimalChargingWindow(hours);
    }
}
