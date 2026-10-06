package com.rumi.structuralmonitoring.application;

import com.rumi.structuralmonitoring.application.command.IngestReadingCommand;
import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import com.rumi.structuralmonitoring.domain.repository.SensorReadingRepository;
import com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq.SensorReadingRecordedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StructuralMonitoringApplicationServiceTest {

    static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    static final UUID SENSOR_ID = UUID.fromString("5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11");
    static final Instant TIMESTAMP = Instant.parse("2026-10-06T15:30:00Z");

    private SensorReadingRepository repository;
    private SensorReadingRecordedPublisher publisher;
    private StructuralMonitoringApplicationService service;

    @BeforeEach
    void setUp() {
        repository = mock(SensorReadingRepository.class);
        publisher = mock(SensorReadingRecordedPublisher.class);
        service = new StructuralMonitoringApplicationService(repository, publisher);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesTheReadingAndPublishesTheVibrationAsTheEventValue() {
        SensorReading reading = service.ingestReading(new IngestReadingCommand(
                SENSOR_ID, BUILDING_ID, "FLOOR-3-NORTH", TIMESTAMP, 0.42, 0.8, 1.2));

        assertThat(reading.getId()).isNotNull();
        assertThat(reading.getMeasurement()).isEqualTo(new SensorMeasurement(0.42, 0.8, 1.2));
        verify(repository).save(reading);

        ArgumentCaptor<SensorReadingRecorded> event = ArgumentCaptor.forClass(SensorReadingRecorded.class);
        verify(publisher).publish(event.capture());
        assertThat(event.getValue().eventId()).isNotNull();
        assertThat(event.getValue().sensorId()).isEqualTo(SENSOR_ID);
        assertThat(event.getValue().buildingId()).isEqualTo(BUILDING_ID);
        assertThat(event.getValue().zone()).isEqualTo("FLOOR-3-NORTH");
        assertThat(event.getValue().recordedAt()).isEqualTo(TIMESTAMP);
        assertThat(event.getValue().value()).isEqualTo(0.42);
    }

    @Test
    void acceptsAReadingWithoutOptionalMeasurements() {
        SensorReading reading = service.ingestReading(new IngestReadingCommand(
                SENSOR_ID, BUILDING_ID, "FLOOR-1-SOUTH", TIMESTAMP, 0.1, null, null));

        assertThat(reading.getMeasurement().inclination()).isNull();
        assertThat(reading.getMeasurement().displacement()).isNull();
    }
}
