package com.rumi.structuralmonitoring.infrastructure.web;

import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Without the dev profile the simulator endpoint must not exist. */
@WebMvcTest
class SimulatedReadingsControllerProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StructuralMonitoringApplicationService applicationService;

    @Test
    void simulatorEndpointIsNotAvailableOutsideTheDevProfile() throws Exception {
        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OpenApiExamples.RECORD_READING_REQUEST))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(applicationService);
    }
}
