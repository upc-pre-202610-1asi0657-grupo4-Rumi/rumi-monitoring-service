package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import com.rumi.structuralmonitoring.domain.model.SensorReading;
import com.rumi.structuralmonitoring.domain.repository.SensorReadingRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TimescaleSensorReadingRepository implements SensorReadingRepository {

    private final SpringDataSensorReadingRepository springDataRepository;

    public TimescaleSensorReadingRepository(SpringDataSensorReadingRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public SensorReading save(SensorReading reading) {
        return springDataRepository.save(SensorReadingEntity.fromDomain(reading)).toDomain();
    }
}
