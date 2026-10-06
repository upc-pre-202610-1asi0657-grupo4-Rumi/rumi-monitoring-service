package com.rumi.structuralmonitoring.infrastructure.web;

import com.rumi.structuralmonitoring.application.StructuralMonitoringApplicationService;
import com.rumi.structuralmonitoring.infrastructure.web.dto.ReadingResponse;
import com.rumi.structuralmonitoring.infrastructure.web.dto.ZoneReadingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/readings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sensor readings", description = "Readings produced by the IoT sensors of a building (US09, US10)")
public class ReadingsController {

    private final StructuralMonitoringApplicationService applicationService;

    public ReadingsController(StructuralMonitoringApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Get the latest reading of each zone of a building",
            description = "US09. Returns the most recent reading of every zone of the building, ordered by "
                    + "zone. The building is owned by rumi-building-service and is not checked here: a building "
                    + "without readings returns an empty list."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Latest reading per zone (empty list if the building has no readings)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ZoneReadingResponse.class)),
                    examples = {
                            @ExampleObject(
                                    name = "latestReadings",
                                    summary = "Two zones of the sample building",
                                    value = OpenApiExamples.LATEST_READINGS_RESPONSE
                            ),
                            @ExampleObject(name = "noReadings", summary = "Building without readings", value = "[]")
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "buildingId is missing or is not a UUID",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                            @ExampleObject(
                                    name = "missingBuildingId",
                                    summary = "buildingId is missing",
                                    value = OpenApiExamples.ERROR_MISSING_BUILDING_ID
                            ),
                            @ExampleObject(
                                    name = "malformedBuildingId",
                                    summary = "buildingId is not a UUID",
                                    value = OpenApiExamples.ERROR_MALFORMED_BUILDING_ID
                            )
                    }
            )
    )
    public List<ZoneReadingResponse> getLatestReadings(
            @Parameter(description = "Building whose zones are queried", required = true,
                    example = OpenApiExamples.BUILDING_ID)
            @RequestParam UUID buildingId
    ) {
        return applicationService.getLatestReadingsByZone(buildingId).stream()
                .map(ZoneReadingResponse::fromDomain)
                .toList();
    }

    @GetMapping
    @Operation(
            summary = "List the readings of one zone of a building",
            description = "US09. Returns the readings of the zone, newest first. limit caps the number of "
                    + "readings (default 100, maximum 500). An unknown building or zone returns an empty list."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Readings of the zone, newest first",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ReadingResponse.class)),
                    examples = @ExampleObject(
                            name = "zoneReadings",
                            summary = "Two most recent readings of FLOOR-3-NORTH",
                            value = OpenApiExamples.ZONE_READINGS_RESPONSE
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "buildingId or zone is missing or invalid, or limit is out of range",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                            @ExampleObject(
                                    name = "missingZone",
                                    summary = "zone is missing",
                                    value = OpenApiExamples.ERROR_MISSING_ZONE
                            ),
                            @ExampleObject(
                                    name = "invalidLimit",
                                    summary = "limit is out of range",
                                    value = OpenApiExamples.ERROR_INVALID_LIMIT
                            )
                    }
            )
    )
    public List<ReadingResponse> getReadingsByZone(
            @Parameter(description = "Building whose zone is queried", required = true,
                    example = OpenApiExamples.BUILDING_ID)
            @RequestParam UUID buildingId,
            @Parameter(description = "Zone of the building, named by floor and wing", required = true,
                    example = OpenApiExamples.ZONE)
            @RequestParam
            @NotBlank(message = "zone is required")
            @Size(max = 50, message = "zone must have at most 50 characters")
            String zone,
            @Parameter(description = "Maximum number of readings to return (1 to 500)", example = "100",
                    schema = @Schema(type = "integer", defaultValue = "100", minimum = "1", maximum = "500"))
            @RequestParam(defaultValue = "100")
            @Min(value = 1, message = "limit must be between 1 and 500")
            @Max(value = StructuralMonitoringApplicationService.MAX_READINGS_LIMIT,
                    message = "limit must be between 1 and 500")
            int limit
    ) {
        return applicationService.getReadingsByZone(buildingId, zone, limit).stream()
                .map(ReadingResponse::fromDomain)
                .toList();
    }
}
