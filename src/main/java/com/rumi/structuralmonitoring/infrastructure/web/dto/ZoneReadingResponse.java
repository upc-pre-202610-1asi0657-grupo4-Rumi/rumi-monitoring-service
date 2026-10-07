package com.rumi.structuralmonitoring.infrastructure.web.dto;

import com.rumi.structuralmonitoring.domain.model.SensorReading;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Latest reading of one zone of the building")
public record ZoneReadingResponse(
        @Schema(description = "Zone of the building", example = "FLOOR-3-NORTH")
        String zone,
        @Schema(description = "When the reading was taken", example = "2026-10-06T15:30:00Z")
        Instant timestamp,
        @Schema(description = "Vibration", example = "0.42")
        double vibration,
        @Schema(description = "Inclination, null when not measured", example = "0.8", nullable = true)
        Double inclination,
        @Schema(description = "Displacement, null when not measured", example = "1.2", nullable = true)
        Double displacement
) {
    public static ZoneReadingResponse fromDomain(SensorReading reading) {
        return new ZoneReadingResponse(
                reading.getZone(),
                reading.getTimestamp(),
                reading.getMeasurement().vibration(),
                reading.getMeasurement().inclination(),
                reading.getMeasurement().displacement()
        );
    }
}
