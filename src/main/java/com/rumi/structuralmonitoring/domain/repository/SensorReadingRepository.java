package com.rumi.structuralmonitoring.domain.repository;

import com.rumi.structuralmonitoring.domain.model.SensorReading;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SensorReadingRepository {

    SensorReading save(SensorReading reading);

    /** The most recent reading of every zone of the building, ordered by zone. */
    List<SensorReading> findLatestPerZone(UUID buildingId);

    /** Readings of one zone of the building, newest first, at most {@code limit} of them. */
    List<SensorReading> findByZone(UUID buildingId, String zone, int limit);

    /** Readings of the building taken between from and to (both inclusive), oldest first. */
    List<SensorReading> findHistory(UUID buildingId, Instant from, Instant to);
}
