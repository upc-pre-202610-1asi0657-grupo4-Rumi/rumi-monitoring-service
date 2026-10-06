package com.rumi.structuralmonitoring.infrastructure.web.dto;

import com.rumi.structuralmonitoring.application.command.IngestReadingCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Sensor reading produced by the IoT simulator")
public record RecordReadingRequest(
        @Schema(description = "Sensor that produced the reading", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "sensorId is required")
        UUID sensorId,

        @Schema(description = "Building where the sensor is installed", example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "buildingId is required")
        UUID buildingId,

        @Schema(description = "Zone of the building, named by floor and wing", example = "FLOOR-3-NORTH",
                maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "zone is required")
        @Size(max = 50, message = "zone must have at most 50 characters")
        String zone,

        @Schema(description = "Vibration measured by the sensor", example = "0.42",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "vibration is required")
        Double vibration,

        @Schema(description = "Inclination measured by the sensor (optional)", example = "0.8")
        Double inclination,

        @Schema(description = "Displacement measured by the sensor (optional)", example = "1.2")
        Double displacement,

        @Schema(description = "When the reading was taken (ISO-8601 instant)", example = "2026-10-06T15:30:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "timestamp is required")
        Instant timestamp
) {
    public IngestReadingCommand toCommand() {
        return new IngestReadingCommand(sensorId, buildingId, zone, timestamp, vibration, inclination, displacement);
    }
}
