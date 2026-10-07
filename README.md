# rumi-monitoring-service

Structural Monitoring service of **Rumi**, the structural monitoring platform by Kuntur Labs.

| | |
|---|---|
| Bounded context | Structural Monitoring |
| Port | `8082` |
| Database | `sensorreadingsdb` (TimescaleDB on PostgreSQL); H2 in memory with the `dev` profile |
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
| GET | `/api/v1/readings/latest` | Latest reading of each zone of a building | `buildingId` (UUID, required) | 200 list of `{zone, timestamp, vibration, inclination, displacement}`, `[]` if none | US09 | Implemented |
| GET | `/api/v1/readings` | Readings of one zone, newest first | `buildingId`, `zone` (required), `limit` (1-500, default 100) | 200 list of readings with `id` and `sensorId` | US09 | Implemented |
| GET | `/api/v1/readings/history` | Readings of a building between two instants, oldest first | `buildingId`, `from`, `to` (ISO-8601, required, inclusive) | 200 list of readings with `id` and `sensorId` | US10 | Implemented |
| POST | `/api/v1/readings` | Record a simulated reading and publish `SensorReadingRecorded` | JSON reading | 201 stored reading with `id` | US09 (simulator) | **Dev profile only**, not a production endpoint |

Production endpoints implemented: 3. The dev-only simulator endpoint is not counted.
The digital twin endpoints (US26) are planned for later sprints.

Errors are RFC 7807 `ProblemDetail` (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "buildingId is required",
  "instance": "/api/v1/readings/latest"
}
```

The building lives in `rumi-building-service` and is not checked here: an unknown building
returns `200 []`.

Sample data used by the examples:

| | |
|---|---|
| Building | `7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d` |
| Sensor | `5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11` |
| Zones | free text named by floor and wing, for example `FLOOR-3-NORTH` (max 50 characters) |

The dev database starts empty: record readings with `POST /api/v1/readings` first, for example

```sh
curl -X POST http://localhost:8082/api/v1/readings -H 'Content-Type: application/json' -d '{
  "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
  "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
  "zone": "FLOOR-3-NORTH",
  "vibration": 0.42, "inclination": 0.8, "displacement": 1.2,
  "timestamp": "2026-10-06T15:30:00Z"
}'
```

## Events

| Event | Direction | Exchange | Routing key |
|---|---|---|---|
| `SensorReadingRecorded` | published | `rumi.structural-monitoring.events` (topic, durable) | `structural-monitoring.sensor-reading.recorded` |

The consumer (`rumi-seismic-service`) reads it from the durable queue
`rumi.seismic-correlation.sensor-reading-recorded`, bound to the exchange with the routing key above.

```json
{
  "eventId": "7c1f5b0e-3a52-4d0b-9f5e-2f6c1a8f4d11",
  "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
  "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
  "zone": "FLOOR-3-NORTH",
  "recordedAt": "2026-10-06T15:30:00Z",
  "value": 0.42
}
```

| Field | Type | Meaning |
|---|---|---|
| `eventId` | UUID | Id of the event, new for every publication |
| `sensorId` | UUID | Sensor that produced the reading |
| `buildingId` | UUID | Building of the sensor |
| `zone` | string, required | Zone of the reading, for example `FLOOR-3-NORTH` |
| `recordedAt` | ISO-8601 instant | When the reading was taken |
| `value` | number | Vibration of the reading |

The event is published right after the reading is stored by `POST /api/v1/readings`. There is no
outbox yet: if the broker is down, the reading stays stored and the request fails with 500.
With `rumi.messaging.enabled=false` (dev profile) the event is only logged.

The contract class is `com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded`.
The consumer (`rumi-seismic-service`) keeps its own copy; a change must be applied on both sides.

## API documentation

- Swagger UI: <http://localhost:8082/swagger-ui.html>
- OpenAPI spec: <http://localhost:8082/v3/api-docs>
- Exported spec: [`docs/openapi.json`](docs/openapi.json), exported from the dev profile, so it also
  documents the dev-only `POST /api/v1/readings`

## Run

Requirements: JDK 21. Maven is not needed, the wrapper downloads it.

Without any infrastructure (H2 in memory, messaging disabled):

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# or
./mvnw package && java -jar target/rumi-monitoring-service-0.1.0.jar --spring.profiles.active=dev
```

Default profile (PostgreSQL / TimescaleDB and RabbitMQ through the variables below):

```sh
./mvnw spring-boot:run
```

The schema is created and updated by Hibernate (`ddl-auto: update`); there is no migration tool yet.

| Variable | Default |
|---|---|
| `SERVER_PORT` | `8082` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USERNAME` | `guest` |
| `RABBITMQ_PASSWORD` | `guest` |
| `MESSAGING_ENABLED` | `true` |
| `MONITORING_DB_URL` | `jdbc:postgresql://localhost:5432/sensorreadingsdb` |
| `MONITORING_DB_USERNAME` | `postgres` |
| `MONITORING_DB_PASSWORD` | `postgres` |

## Test

```sh
./mvnw test
```

The tests need neither a database nor a message broker: persistence tests run on H2.

## Structure

```
com.rumi.structuralmonitoring
├── application                  StructuralMonitoringApplicationService (use cases)
├── domain
│   ├── event                    SensorReadingRecorded contract
│   ├── model                    SensorReading, SensorMeasurement
│   └── repository               SensorReadingRepository (port)
└── infrastructure
    ├── messaging.rabbitmq       publisher and RabbitMQ configuration
    ├── persistence.jpa          sensor_readings entity and TimescaleSensorReadingRepository
    └── web                      REST controllers, DTOs, ProblemDetail handler, OpenAPI
```

## Known limitations

- Only the dev simulator records readings; the ingestion from the real sensor gateway is pending.
- `sensor_readings` is not converted into a TimescaleDB hypertable automatically.
- No outbox between storing a reading and publishing its event.
- `GET /api/v1/readings/history` is not paginated.
- Baselines and the digital twin (`ZoneBaselineProfile`, `ZoneRiskSnapshot`) are not implemented yet.
