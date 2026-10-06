package com.rumi.structuralmonitoring.infrastructure.web;

/**
 * Example payloads shown in the OpenAPI documentation. They use the sample building and
 * sensor of the development profile.
 */
public final class OpenApiExamples {

    public static final String BUILDING_ID = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d";
    public static final String SENSOR_ID = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11";
    public static final String ZONE = "FLOOR-3-NORTH";
    public static final String PROBLEM_JSON = "application/problem+json";

    public static final String RECORD_READING_REQUEST = """
            {
              "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "zone": "FLOOR-3-NORTH",
              "vibration": 0.42,
              "inclination": 0.8,
              "displacement": 1.2,
              "timestamp": "2026-10-06T15:30:00Z"
            }""";

    public static final String RECORDED_READING_RESPONSE = """
            {
              "id": "e4b7c2a9-1f3d-4c8e-9a6b-2d5f8e1c7b30",
              "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "zone": "FLOOR-3-NORTH",
              "vibration": 0.42,
              "inclination": 0.8,
              "displacement": 1.2,
              "timestamp": "2026-10-06T15:30:00Z"
            }""";

    public static final String ERROR_INVALID_BODY = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "zone is required",
              "instance": "/api/v1/readings"
            }""";

    public static final String ERROR_MALFORMED_BODY_FIELD = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "sensorId must be a valid UUID",
              "instance": "/api/v1/readings"
            }""";

    private OpenApiExamples() {
    }
}
