package com.rumi.structuralmonitoring.domain.model;

/**
 * Values measured by a sensor at one instant. Vibration is always present; inclination and
 * displacement are optional because not every sensor measures them.
 */
public record SensorMeasurement(
        double vibration,
        Double inclination,
        Double displacement
) {
    public SensorMeasurement {
        if (!Double.isFinite(vibration)) {
            throw new IllegalArgumentException("Vibration must be a finite number");
        }
    }
}
