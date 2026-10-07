package com.rumi.structuralmonitoring.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Published every time a sensor reading is recorded. {@code value} carries the vibration
 * of the reading. The consumer (rumi-seismic-service) keeps its own copy of this contract.
 */
public record SensorReadingRecorded(
        UUID eventId,
        UUID sensorId,
        UUID buildingId,
        String zone,
        Instant recordedAt,
        double value
) {
    public SensorReadingRecorded {
        Objects.requireNonNull(eventId, "Event id is required");
        Objects.requireNonNull(sensorId, "Sensor id is required");
        Objects.requireNonNull(buildingId, "Building id is required");
        Objects.requireNonNull(zone, "Zone is required");
        Objects.requireNonNull(recordedAt, "Recording time is required");
    }
}
