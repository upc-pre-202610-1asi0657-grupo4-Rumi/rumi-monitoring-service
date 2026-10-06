package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataSensorReadingRepository
        extends JpaRepository<SensorReadingEntity, SensorReadingEntityId> {

    @Query("""
            select r from SensorReadingEntity r
            where r.buildingId = :buildingId
              and r.timestamp = (
                  select max(latest.timestamp) from SensorReadingEntity latest
                  where latest.buildingId = :buildingId and latest.zone = r.zone)
            order by r.zone, r.sensorId
            """)
    List<SensorReadingEntity> findLatestPerZone(@Param("buildingId") UUID buildingId);
}
