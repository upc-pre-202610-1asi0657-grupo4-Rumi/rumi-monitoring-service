package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSensorReadingRepository
        extends JpaRepository<SensorReadingEntity, SensorReadingEntityId> {
}
