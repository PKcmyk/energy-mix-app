package com.codibly.energymix.controller;

import com.codibly.energymix.dto.ChargingWindowDto;
import com.codibly.energymix.dto.DailyMixDto;
import com.codibly.energymix.service.EnergyMixService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EnergyMixController {

    private final EnergyMixService service;

    public EnergyMixController(EnergyMixService service) {
        this.service = service;
    }

    @GetMapping("/energy-mix")
    public List<DailyMixDto> getEnergyMix() {
        return service.getThreeDayMix();
    }

    @GetMapping("/charging-window")
    public ChargingWindowDto getChargingWindow(@RequestParam @Min(1) @Max(6) int hours) {
        return service.getOptimalChargingWindow(hours);
    }
}
