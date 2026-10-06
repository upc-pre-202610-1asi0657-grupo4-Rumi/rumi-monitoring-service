package com.rumi.structuralmonitoring.application;

import com.rumi.structuralmonitoring.application.command.IngestReadingCommand;
import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import com.rumi.structuralmonitoring.domain.repository.SensorReadingRepository;
import com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq.SensorReadingRecordedPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StructuralMonitoringApplicationService {

    private final SensorReadingRepository readingRepository;
    private final SensorReadingRecordedPublisher eventPublisher;

    public StructuralMonitoringApplicationService(
            SensorReadingRepository readingRepository,
            SensorReadingRecordedPublisher eventPublisher
    ) {
        this.readingRepository = readingRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Stores the reading and then publishes SensorReadingRecorded with the vibration as its value.
     * There is no outbox yet: if the broker fails, the reading stays stored and the error propagates.
     */
    public SensorReading ingestReading(IngestReadingCommand command) {
        SensorReading reading = readingRepository.save(SensorReading.record(
                command.sensorId(),
                command.buildingId(),
                command.zone(),
                command.timestamp(),
                new SensorMeasurement(command.vibration(), command.inclination(), command.displacement())
        ));
        eventPublisher.publish(new SensorReadingRecorded(
                UUID.randomUUID(),
                reading.getSensorId(),
                reading.getBuildingId(),
                reading.getZone(),
                reading.getTimestamp(),
                reading.getMeasurement().vibration()
        ));
        return reading;
    }

    /** US09: current state of the building, one reading per zone. Empty if the building has no readings. */
    public List<SensorReading> getLatestReadingsByZone(UUID buildingId) {
        return readingRepository.findLatestPerZone(buildingId);
    }
}
