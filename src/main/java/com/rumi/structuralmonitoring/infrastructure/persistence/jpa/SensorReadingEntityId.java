package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key of sensor_readings. TimescaleDB requires the partitioning column
 * (timestamp) to be part of every unique index of a hypertable.
 */
public class SensorReadingEntityId implements Serializable {

    private UUID id;
    private Instant timestamp;

    protected SensorReadingEntityId() {
    }

    public SensorReadingEntityId(UUID id, Instant timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SensorReadingEntityId that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp);
    }
}
