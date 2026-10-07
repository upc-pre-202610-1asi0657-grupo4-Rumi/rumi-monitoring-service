package com.rumi.structuralmonitoring.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SensorReading {

    private final UUID id;
    private final UUID sensorId;
    private final UUID buildingId;
    private final String zone;
    private final Instant timestamp;
    private final SensorMeasurement measurement;

    public SensorReading(
            UUID id,
            UUID sensorId,
            UUID buildingId,
            String zone,
            Instant timestamp,
            SensorMeasurement measurement
    ) {
        this.id = Objects.requireNonNull(id, "Reading id is required");
        this.sensorId = Objects.requireNonNull(sensorId, "Sensor id is required");
        this.buildingId = Objects.requireNonNull(buildingId, "Building id is required");
        this.zone = Objects.requireNonNull(zone, "Zone is required");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp is required");
        this.measurement = Objects.requireNonNull(measurement, "Measurement is required");
        if (zone.isBlank()) {
            throw new IllegalArgumentException("Zone must not be blank");
        }
    }

    public static SensorReading record(
            UUID sensorId,
            UUID buildingId,
            String zone,
            Instant timestamp,
            SensorMeasurement measurement
    ) {
        return new SensorReading(UUID.randomUUID(), sensorId, buildingId, zone, timestamp, measurement);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public String getZone() {
        return zone;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public SensorMeasurement getMeasurement() {
        return measurement;
    }
}
