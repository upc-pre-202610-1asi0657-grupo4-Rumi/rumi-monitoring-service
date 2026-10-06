package com.rumi.structuralmonitoring.domain.repository;

import com.rumi.structuralmonitoring.domain.model.SensorReading;

import java.util.List;
import java.util.UUID;

public interface SensorReadingRepository {

    SensorReading save(SensorReading reading);

    /** The most recent reading of every zone of the building, ordered by zone. */
    List<SensorReading> findLatestPerZone(UUID buildingId);
}
