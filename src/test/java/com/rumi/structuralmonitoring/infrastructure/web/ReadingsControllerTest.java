package com.rumi.structuralmonitoring.infrastructure.web;

import com.rumi.structuralmonitoring.application.InvalidQueryException;
import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingsController.class)
class ReadingsControllerTest {

    static final UUID BUILDING_ID = UUID.fromString(OpenApiExamples.BUILDING_ID);
    static final UUID SENSOR_ID = UUID.fromString(OpenApiExamples.SENSOR_ID);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StructuralMonitoringApplicationService applicationService;

    @Test
    void returnsTheLatestReadingOfEachZone() throws Exception {
        when(applicationService.getLatestReadingsByZone(BUILDING_ID)).thenReturn(List.of(new SensorReading(
                UUID.randomUUID(), SENSOR_ID, BUILDING_ID, "FLOOR-3-NORTH",
                Instant.parse("2026-10-06T15:30:00Z"), new SensorMeasurement(0.42, 0.8, 1.2))));

        mockMvc.perform(get("/api/v1/readings/latest").param("buildingId", OpenApiExamples.BUILDING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].zone").value("FLOOR-3-NORTH"))
                .andExpect(jsonPath("$[0].timestamp").value("2026-10-06T15:30:00Z"))
                .andExpect(jsonPath("$[0].vibration").value(0.42))
                .andExpect(jsonPath("$[0].inclination").value(0.8))
                .andExpect(jsonPath("$[0].displacement").value(1.2))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void returnsAnEmptyListWhenTheBuildingHasNoReadings() throws Exception {
        when(applicationService.getLatestReadingsByZone(BUILDING_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/readings/latest").param("buildingId", OpenApiExamples.BUILDING_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void returns400WhenBuildingIdIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/readings/latest"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("buildingId is required"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenBuildingIdIsNotAUuid() throws Exception {
        mockMvc.perform(get("/api/v1/readings/latest").param("buildingId", "building-7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("buildingId must be a valid UUID"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returnsTheReadingsOfAZoneWithTheDefaultLimit() throws Exception {
        UUID id = UUID.fromString("e4b7c2a9-1f3d-4c8e-9a6b-2d5f8e1c7b30");
        when(applicationService.getReadingsByZone(BUILDING_ID, "FLOOR-3-NORTH", 100)).thenReturn(List.of(
                new SensorReading(id, SENSOR_ID, BUILDING_ID, "FLOOR-3-NORTH",
                        Instant.parse("2026-10-06T15:30:00Z"), new SensorMeasurement(0.42, null, 1.2))));

        mockMvc.perform(get("/api/v1/readings")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("zone", "FLOOR-3-NORTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].sensorId").value(OpenApiExamples.SENSOR_ID))
                .andExpect(jsonPath("$[0].zone").value("FLOOR-3-NORTH"))
                .andExpect(jsonPath("$[0].timestamp").value("2026-10-06T15:30:00Z"))
                .andExpect(jsonPath("$[0].vibration").value(0.42))
                .andExpect(jsonPath("$[0].inclination").isEmpty())
                .andExpect(jsonPath("$[0].displacement").value(1.2));
    }

    @Test
    void passesTheRequestedLimit() throws Exception {
        when(applicationService.getReadingsByZone(BUILDING_ID, "FLOOR-3-NORTH", 500)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/readings")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("zone", "FLOOR-3-NORTH")
                        .param("limit", "500"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void returns400WhenZoneIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/readings").param("buildingId", OpenApiExamples.BUILDING_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("zone is required"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenZoneIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/readings")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("zone", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("zone is required"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenLimitIsOutOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/readings")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("zone", "FLOOR-3-NORTH")
                        .param("limit", "501"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("limit must be between 1 and 500"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenLimitIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/v1/readings")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("zone", "FLOOR-3-NORTH")
                        .param("limit", "ten"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("limit must be a number"));
    }

    @Test
    void returns400WhenBuildingIdIsMissingFromTheZoneQuery() throws Exception {
        mockMvc.perform(get("/api/v1/readings").param("zone", "FLOOR-3-NORTH"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("buildingId is required"));
    }

    @Test
    void returnsTheHistoryOfTheBuilding() throws Exception {
        Instant from = Instant.parse("2026-10-06T15:00:00Z");
        Instant to = Instant.parse("2026-10-06T16:00:00Z");
        when(applicationService.getHistory(BUILDING_ID, from, to)).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), SENSOR_ID, BUILDING_ID, "FLOOR-3-NORTH",
                        Instant.parse("2026-10-06T15:30:00Z"), new SensorMeasurement(0.42, 0.8, 1.2))));

        mockMvc.perform(get("/api/v1/readings/history")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("from", "2026-10-06T15:00:00Z")
                        .param("to", "2026-10-06T16:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sensorId").value(OpenApiExamples.SENSOR_ID))
                .andExpect(jsonPath("$[0].timestamp").value("2026-10-06T15:30:00Z"));
    }

    @Test
    void returns400WhenFromIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/readings/history")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("to", "2026-10-06T16:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("from is required"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenToIsMalformed() throws Exception {
        mockMvc.perform(get("/api/v1/readings/history")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("from", "2026-10-06T15:00:00Z")
                        .param("to", "yesterday"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("to must be an ISO-8601 instant, for example 2026-10-06T15:30:00Z"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenFromIsAfterTo() throws Exception {
        when(applicationService.getHistory(eq(BUILDING_ID), any(), any()))
                .thenThrow(new InvalidQueryException("from must not be after to"));

        mockMvc.perform(get("/api/v1/readings/history")
                        .param("buildingId", OpenApiExamples.BUILDING_ID)
                        .param("from", "2026-10-06T16:00:00Z")
                        .param("to", "2026-10-06T15:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("from must not be after to"));
    }
}
