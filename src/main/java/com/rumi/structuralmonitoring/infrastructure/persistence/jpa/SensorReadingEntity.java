package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@IdClass(SensorReadingEntityId.class)
@Table(
        name = "sensor_readings",
        indexes = @Index(name = "ix_sensor_readings_building_zone_timestamp",
                columnList = "building_id, zone, timestamp")
)
public class SensorReadingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Id
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "zone", nullable = false, length = 50)
    private String zone;

    @Column(name = "vibration")
    private Double vibration;

    @Column(name = "inclination")
    private Double inclination;

    @Column(name = "displacement")
    private Double displacement;

    protected SensorReadingEntity() {
    }

    public static SensorReadingEntity fromDomain(SensorReading reading) {
        SensorReadingEntity entity = new SensorReadingEntity();
        entity.id = reading.getId();
        entity.timestamp = reading.getTimestamp();
        entity.sensorId = reading.getSensorId();
        entity.buildingId = reading.getBuildingId();
        entity.zone = reading.getZone();
        entity.vibration = reading.getMeasurement().vibration();
        entity.inclination = reading.getMeasurement().inclination();
        entity.displacement = reading.getMeasurement().displacement();
        return entity;
    }

    public SensorReading toDomain() {
        return new SensorReading(
                id,
                sensorId,
                buildingId,
                zone,
                timestamp,
                new SensorMeasurement(vibration, inclination, displacement)
        );
    }
}
