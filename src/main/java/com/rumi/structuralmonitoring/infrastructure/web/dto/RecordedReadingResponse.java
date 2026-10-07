package com.rumi.structuralmonitoring.infrastructure.web.dto;

import com.rumi.structuralmonitoring.domain.model.SensorReading;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Sensor reading as stored by the service")
public record RecordedReadingResponse(
        @Schema(description = "Reading id", example = "e4b7c2a9-1f3d-4c8e-9a6b-2d5f8e1c7b30")
        UUID id,
        @Schema(description = "Sensor that produced the reading", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11")
        UUID sensorId,
        @Schema(description = "Building where the sensor is installed", example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d")
        UUID buildingId,
        @Schema(description = "Zone of the building", example = "FLOOR-3-NORTH")
        String zone,
        @Schema(description = "Vibration", example = "0.42")
        double vibration,
        @Schema(description = "Inclination, null when not measured", example = "0.8", nullable = true)
        Double inclination,
        @Schema(description = "Displacement, null when not measured", example = "1.2", nullable = true)
        Double displacement,
        @Schema(description = "When the reading was taken", example = "2026-10-06T15:30:00Z")
        Instant timestamp
) {
    public static RecordedReadingResponse fromDomain(SensorReading reading) {
        return new RecordedReadingResponse(
                reading.getId(),
                reading.getSensorId(),
                reading.getBuildingId(),
                reading.getZone(),
                reading.getMeasurement().vibration(),
                reading.getMeasurement().inclination(),
                reading.getMeasurement().displacement(),
                reading.getTimestamp()
        );
    }
}
