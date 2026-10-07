package com.rumi.structuralmonitoring.application.command;

import java.time.Instant;
import java.util.UUID;

public record IngestReadingCommand(
        UUID sensorId,
        UUID buildingId,
        String zone,
        Instant timestamp,
        double vibration,
        Double inclination,
        Double displacement
) {
}
