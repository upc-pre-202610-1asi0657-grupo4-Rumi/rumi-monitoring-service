package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TimescaleSensorReadingRepository.class)
class TimescaleSensorReadingRepositoryTest {

    static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    static final UUID SENSOR_ID = UUID.fromString("5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11");

    @Autowired
    private TimescaleSensorReadingRepository repository;

    @Autowired
    private SpringDataSensorReadingRepository springDataRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAReadingWithAllItsColumns() {
        SensorReading reading = SensorReading.record(
                SENSOR_ID,
                BUILDING_ID,
                "FLOOR-3-NORTH",
                Instant.parse("2026-10-06T15:30:00Z"),
                new SensorMeasurement(0.42, 0.8, 1.2)
        );

        repository.save(reading);
        entityManager.flush();
        entityManager.clear();

        SensorReading stored = springDataRepository
                .findById(new SensorReadingEntityId(reading.getId(), reading.getTimestamp()))
                .orElseThrow()
                .toDomain();
        assertThat(stored.getSensorId()).isEqualTo(SENSOR_ID);
        assertThat(stored.getBuildingId()).isEqualTo(BUILDING_ID);
        assertThat(stored.getZone()).isEqualTo("FLOOR-3-NORTH");
        assertThat(stored.getTimestamp()).isEqualTo(Instant.parse("2026-10-06T15:30:00Z"));
        assertThat(stored.getMeasurement()).isEqualTo(new SensorMeasurement(0.42, 0.8, 1.2));
    }

    @Test
    void savesAReadingWithoutOptionalMeasurements() {
        SensorReading reading = SensorReading.record(
                SENSOR_ID, BUILDING_ID, "FLOOR-1-SOUTH",
                Instant.parse("2026-10-06T15:31:00Z"),
                new SensorMeasurement(0.1, null, null)
        );

        repository.save(reading);
        entityManager.flush();
        entityManager.clear();

        SensorReading stored = springDataRepository
                .findById(new SensorReadingEntityId(reading.getId(), reading.getTimestamp()))
                .orElseThrow()
                .toDomain();
        assertThat(stored.getMeasurement().inclination()).isNull();
        assertThat(stored.getMeasurement().displacement()).isNull();
    }
}
