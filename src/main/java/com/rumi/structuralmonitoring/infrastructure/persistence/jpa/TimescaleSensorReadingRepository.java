package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import com.rumi.structuralmonitoring.domain.model.SensorReading;
import com.rumi.structuralmonitoring.domain.repository.SensorReadingRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public List<SensorReading> findLatestPerZone(UUID buildingId) {
        // Two sensors of the same zone may report at the same instant: keep one reading per zone.
        Map<String, SensorReading> latestByZone = new LinkedHashMap<>();
        springDataRepository.findLatestPerZone(buildingId).stream()
                .map(SensorReadingEntity::toDomain)
                .forEach(reading -> latestByZone.putIfAbsent(reading.getZone(), reading));
        return List.copyOf(latestByZone.values());
    }
}
