package com.rumi.structuralmonitoring.domain.repository;

import com.rumi.structuralmonitoring.domain.model.SensorReading;

public interface SensorReadingRepository {

    SensorReading save(SensorReading reading);
}
