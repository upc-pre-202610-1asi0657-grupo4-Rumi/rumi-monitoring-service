package com.rumi.structuralmonitoring.infrastructure.web;

import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import com.rumi.structuralmonitoring.domain.model.SensorMeasurement;
import com.rumi.structuralmonitoring.domain.model.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulatedReadingsController.class)
@ActiveProfiles("dev")
class SimulatedReadingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StructuralMonitoringApplicationService applicationService;

    @Test
    void returns201WithTheStoredReading() throws Exception {
        UUID id = UUID.fromString("e4b7c2a9-1f3d-4c8e-9a6b-2d5f8e1c7b30");
        when(applicationService.ingestReading(any())).thenReturn(new SensorReading(
                id,
                UUID.fromString(OpenApiExamples.SENSOR_ID),
                UUID.fromString(OpenApiExamples.BUILDING_ID),
                "FLOOR-3-NORTH",
                Instant.parse("2026-10-06T15:30:00Z"),
                new SensorMeasurement(0.42, 0.8, 1.2)
        ));

        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OpenApiExamples.RECORD_READING_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.sensorId").value(OpenApiExamples.SENSOR_ID))
                .andExpect(jsonPath("$.buildingId").value(OpenApiExamples.BUILDING_ID))
                .andExpect(jsonPath("$.zone").value("FLOOR-3-NORTH"))
                .andExpect(jsonPath("$.vibration").value(0.42))
                .andExpect(jsonPath("$.inclination").value(0.8))
                .andExpect(jsonPath("$.displacement").value(1.2))
                .andExpect(jsonPath("$.timestamp").value("2026-10-06T15:30:00Z"));
    }

    @Test
    void returns400WhenARequiredFieldIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OpenApiExamples.RECORD_READING_REQUEST.replace("\"zone\": \"FLOOR-3-NORTH\",", "")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("zone is required"));
        verifyNoInteractions(applicationService);
    }

    @Test
    void returns400WhenAFieldIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OpenApiExamples.RECORD_READING_REQUEST.replace(OpenApiExamples.SENSOR_ID, "not-a-uuid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("sensorId must be a valid UUID"));
    }

    @Test
    void returns400WhenTheBodyIsNotJson() throws Exception {
        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body is missing or is not valid JSON"));
    }
}
