package com.rumi.structuralmonitoring.integration;

import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import com.rumi.structuralmonitoring.application.command.IngestReadingCommand;
import com.rumi.structuralmonitoring.infrastructure.web.dto.ReadingResponse;
import com.rumi.structuralmonitoring.infrastructure.web.dto.ZoneReadingResponse;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class MonitoringReadingSteps {

    private static final PostgreSQLContainer<?> TIMESCALE = new PostgreSQLContainer<>(
            DockerImageName.parse("timescale/timescaledb:2.17.2-pg16").asCompatibleSubstituteFor("postgres")
    )
            .withEnv("POSTGRES_DB", "monitoring_test")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test")
            .withInitScript("db/timescale-init.sql")
            .withExposedPorts(5432);

    static {
        TIMESCALE.start();
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", TIMESCALE::getJdbcUrl);
        properties.add("spring.datasource.username", TIMESCALE::getUsername);
        properties.add("spring.datasource.password", TIMESCALE::getPassword);
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        properties.add("rumi.timescale.hypertable-initialization", () -> "true");
        properties.add("rumi.messaging.enabled", () -> "false");
    }

    private static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    private static final UUID SENSOR_NORTH = UUID.fromString("5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11");
    private static final UUID SENSOR_SOUTH = UUID.fromString("6e2d3a1f-9b5c-4b64-8a1f-1a2b3c4d5e6f");

    @Autowired
    private StructuralMonitoringApplicationService applicationService;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ResponseEntity<List<ZoneReadingResponse>> latestResponse;
    private ResponseEntity<List<ReadingResponse>> historyResponse;

    @Before
    public void clearDatabase() {
        jdbcTemplate.update("DELETE FROM sensor_readings");
    }

    @Given("the monitoring database contains readings for two zones and multiple timestamps")
    public void seedLatestReadings() {
        save(SENSOR_NORTH, "FLOOR-3-NORTH", "2026-10-06T15:00:00Z", 0.2);
        save(SENSOR_NORTH, "FLOOR-3-NORTH", "2026-10-06T15:30:00Z", 0.42);
        save(SENSOR_SOUTH, "FLOOR-2-SOUTH", "2026-10-06T15:15:00Z", 0.31);
    }

    @When("I request the latest readings for the building")
    public void requestLatestReadings() {
        latestResponse = http.exchange(
                "/api/v1/readings/latest?buildingId=" + BUILDING_ID,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() { }
        );
    }

    @Then("the response contains the newest reading for each zone")
    public void verifyLatestReadings() {
        assertThat(latestResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(latestResponse.getBody()).hasSize(2);
        assertThat(latestResponse.getBody()).extracting(ZoneReadingResponse::zone)
                .containsExactly("FLOOR-2-SOUTH", "FLOOR-3-NORTH");
        assertThat(latestResponse.getBody()).filteredOn(reading -> reading.zone().equals("FLOOR-3-NORTH"))
                .extracting(ZoneReadingResponse::vibration).containsExactly(0.42);
    }

    @Given("the monitoring database contains readings inside and outside a date range")
    public void seedHistoryReadings() {
        save(SENSOR_NORTH, "FLOOR-3-NORTH", "2026-10-06T14:59:59Z", 0.1);
        save(SENSOR_NORTH, "FLOOR-3-NORTH", "2026-10-06T15:00:00Z", 0.2);
        save(SENSOR_SOUTH, "FLOOR-2-SOUTH", "2026-10-06T15:30:00Z", 0.3);
        save(SENSOR_NORTH, "FLOOR-3-NORTH", "2026-10-06T16:00:00Z", 0.4);
        save(SENSOR_SOUTH, "FLOOR-2-SOUTH", "2026-10-06T16:00:01Z", 0.5);
    }

    @When("I request the reading history for that date range")
    public void requestHistory() {
        historyResponse = http.exchange(
                "/api/v1/readings/history?buildingId=" + BUILDING_ID
                        + "&from=2026-10-06T15:00:00Z&to=2026-10-06T16:00:00Z",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() { }
        );
    }

    @Then("the response contains only readings in the range in chronological order")
    public void verifyHistory() {
        assertThat(historyResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(historyResponse.getBody()).hasSize(3);
        assertThat(historyResponse.getBody()).extracting(ReadingResponse::timestamp)
                .containsExactly(
                        Instant.parse("2026-10-06T15:00:00Z"),
                        Instant.parse("2026-10-06T15:30:00Z"),
                        Instant.parse("2026-10-06T16:00:00Z")
                );
    }

    private void save(UUID sensorId, String zone, String timestamp, double vibration) {
        applicationService.ingestReading(new IngestReadingCommand(
                sensorId, BUILDING_ID, zone, Instant.parse(timestamp), vibration, 0.8, 1.2
        ));
    }
}
