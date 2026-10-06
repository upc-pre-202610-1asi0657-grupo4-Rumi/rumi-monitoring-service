package com.rumi.structuralmonitoring.infrastructure.web;

import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import com.rumi.structuralmonitoring.infrastructure.web.dto.RecordReadingRequest;
import com.rumi.structuralmonitoring.infrastructure.web.dto.RecordedReadingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for the IoT simulator. Only registered in the dev profile; in production the
 * readings arrive from the sensor gateway.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v1/readings")
@Tag(name = "Sensor readings", description = "Readings produced by the IoT sensors of a building (US09, US10)")
public class SimulatedReadingsController {

    private final StructuralMonitoringApplicationService applicationService;

    public SimulatedReadingsController(StructuralMonitoringApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Record a simulated sensor reading (dev profile only)",
            description = "Stores a reading sent by the IoT simulator and publishes the SensorReadingRecorded "
                    + "event, whose value is the vibration of the reading. When messaging is disabled (dev "
                    + "profile) the event is only logged. This endpoint exists only when the service runs "
                    + "with the dev profile and is not part of the production API.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Reading to record. inclination and displacement are optional.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RecordReadingRequest.class),
                            examples = @ExampleObject(
                                    name = "reading",
                                    summary = "Reading of the sample sensor",
                                    value = OpenApiExamples.RECORD_READING_REQUEST
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Reading stored and event published",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RecordedReadingResponse.class),
                    examples = @ExampleObject(
                            name = "recorded",
                            summary = "Stored reading with its generated id",
                            value = OpenApiExamples.RECORDED_READING_RESPONSE
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The body is missing, is not valid JSON or a field is missing or malformed",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                            @ExampleObject(
                                    name = "missingField",
                                    summary = "A required field is missing",
                                    value = OpenApiExamples.ERROR_INVALID_BODY
                            ),
                            @ExampleObject(
                                    name = "malformedField",
                                    summary = "A field has the wrong format",
                                    value = OpenApiExamples.ERROR_MALFORMED_BODY_FIELD
                            )
                    }
            )
    )
    public RecordedReadingResponse recordReading(@Valid @RequestBody RecordReadingRequest request) {
        return RecordedReadingResponse.fromDomain(applicationService.ingestReading(request.toCommand()));
    }
}
