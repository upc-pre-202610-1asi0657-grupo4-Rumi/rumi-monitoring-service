# rumi-monitoring-service

Structural Monitoring service of **Rumi**, the structural monitoring platform by Kuntur Labs.

| | |
|---|---|
| Bounded context | Structural Monitoring |
| Port | `8082` |
| Database | `sensorreadingsdb` (TimescaleDB on PostgreSQL), reserved, not connected yet |
| Messaging | RabbitMQ, publisher |
| Gateway routes | `/api/v1/readings/**`, `/api/v1/digital-twin/**` |
| Base package | `com.rumi.structuralmonitoring` |

## Purpose

Owns the readings produced by the IoT sensors installed in a building and tells the rest of
the platform about them through the `SensorReadingRecorded` event.

## Origin

Extracted from the modular monolith
[`rumi-backend`](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-backend) at commit
`rumi-backend@3ab07ec` (branch `develop`), with the history of this context preserved.
The state of the monolith before the decomposition is the tag
[`monolith-baseline`](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-backend/tree/monolith-baseline).

## Endpoints

| Verb | Path | Description | Request | Response | User story | Status |
|---|---|---|---|---|---|---|
| none | | No REST endpoint is implemented yet | | | | |

Implemented functional endpoints: 0. The readings (US09, US10) and digital twin (US26)
endpoints are planned for later sprints.

## Events

| Event | Direction | Exchange | Routing key |
|---|---|---|---|
| `SensorReadingRecorded` | published | `rumi.structural-monitoring.events` (topic, durable) | `structural-monitoring.sensor-reading.recorded` |

```json
{
  "eventId": "7c1f5b0e-3a52-4d0b-9f5e-2f6c1a8f4d11",
  "sensorId": "b2a9d0f4-6c1e-4a57-8f0a-91d2c3e4f5a6",
  "buildingId": "3f2c8a10-5d7b-4e9a-b1c2-0a1b2c3d4e5f",
  "recordedAt": "2026-10-06T15:30:00Z",
  "value": 0.018
}
```

The contract class is `com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded`.
The consumer (`rumi-seismic-service`) keeps its own copy; a change must be applied on both sides.

## API documentation

- Swagger UI: <http://localhost:8082/swagger-ui.html>
- OpenAPI spec: <http://localhost:8082/v3/api-docs>
- Exported spec: [`docs/openapi.json`](docs/openapi.json) (no paths yet)

## Run

Requirements: JDK 21, Maven. RabbitMQ is needed to publish events; the service starts without it.

```sh
mvn spring-boot:run
```

| Variable | Default |
|---|---|
| `SERVER_PORT` | `8082` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USERNAME` | `guest` |
| `RABBITMQ_PASSWORD` | `guest` |
| `MONITORING_DB_URL` | `jdbc:postgresql://localhost:5432/sensorreadingsdb` (reserved) |
| `MONITORING_DB_USERNAME` | `postgres` (reserved) |
| `MONITORING_DB_PASSWORD` | `postgres` (reserved) |

## Test

```sh
mvn test
```

The tests need neither a database nor a message broker.

## Structure

```
com.rumi.structuralmonitoring
├── domain
│   └── event                    SensorReadingRecorded contract
└── infrastructure
    ├── messaging.rabbitmq       publisher and RabbitMQ configuration
    └── web                      OpenAPI configuration
```

## Known limitations

- `SensorReadingRecordedPublisher#publish` has no caller yet: nothing records readings.
