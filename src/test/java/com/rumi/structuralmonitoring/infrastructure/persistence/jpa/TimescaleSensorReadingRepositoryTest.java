package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
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

    @Test
    void findsTheLatestReadingOfEachZoneOfTheBuilding() {
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:00:00Z", 0.10);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.42);
        save(BUILDING_ID, "FLOOR-2-NORTH", "2026-10-06T15:20:00Z", 0.31);
        save(BUILDING_ID, "FLOOR-2-NORTH", "2026-10-06T15:10:00Z", 0.20);
        save(UUID.randomUUID(), "FLOOR-3-NORTH", "2026-10-06T16:00:00Z", 0.99);
        entityManager.flush();
        entityManager.clear();

        List<SensorReading> latest = repository.findLatestPerZone(BUILDING_ID);

        assertThat(latest).extracting(SensorReading::getZone).containsExactly("FLOOR-2-NORTH", "FLOOR-3-NORTH");
        assertThat(latest).extracting(reading -> reading.getMeasurement().vibration()).containsExactly(0.31, 0.42);
    }

    @Test
    void keepsOneReadingPerZoneWhenTwoSensorsReportAtTheSameInstant() {
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.42);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.40);
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findLatestPerZone(BUILDING_ID)).hasSize(1);
    }

    @Test
    void findsNoLatestReadingsForABuildingWithoutReadings() {
        assertThat(repository.findLatestPerZone(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findsTheReadingsOfAZoneNewestFirstUpToTheLimit() {
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:00:00Z", 0.10);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.42);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:20:00Z", 0.30);
        save(BUILDING_ID, "FLOOR-2-NORTH", "2026-10-06T15:40:00Z", 0.50);
        entityManager.flush();
        entityManager.clear();

        List<SensorReading> readings = repository.findByZone(BUILDING_ID, "FLOOR-3-NORTH", 2);

        assertThat(readings).extracting(SensorReading::getTimestamp).containsExactly(
                Instant.parse("2026-10-06T15:30:00Z"), Instant.parse("2026-10-06T15:20:00Z"));
    }

    @Test
    void findsTheHistoryOfTheBuildingOldestFirstWithInclusiveBounds() {
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T14:59:59Z", 0.01);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T16:00:00Z", 0.60);
        save(BUILDING_ID, "FLOOR-2-NORTH", "2026-10-06T15:30:00Z", 0.30);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T15:00:00Z", 0.10);
        save(BUILDING_ID, "FLOOR-3-NORTH", "2026-10-06T16:00:01Z", 0.70);
        save(UUID.randomUUID(), "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.99);
        entityManager.flush();
        entityManager.clear();

        List<SensorReading> history = repository.findHistory(
                BUILDING_ID, Instant.parse("2026-10-06T15:00:00Z"), Instant.parse("2026-10-06T16:00:00Z"));

        assertThat(history).extracting(reading -> reading.getMeasurement().vibration())
                .containsExactly(0.10, 0.30, 0.60);
    }

    private void save(UUID buildingId, String zone, String timestamp, double vibration) {
        repository.save(SensorReading.record(
                UUID.randomUUID(), buildingId, zone, Instant.parse(timestamp),
                new SensorMeasurement(vibration, null, null)));
    }
}
