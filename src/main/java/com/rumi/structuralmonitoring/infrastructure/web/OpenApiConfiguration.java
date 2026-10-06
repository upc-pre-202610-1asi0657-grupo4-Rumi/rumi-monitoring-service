package com.rumi.structuralmonitoring.infrastructure.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI monitoringServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Rumi Monitoring Service API")
                .description("REST API of the Structural Monitoring bounded context: sensor readings "
                        + "(US09, US10). Every recorded reading is announced with the SensorReadingRecorded "
                        + "event over RabbitMQ. Errors use RFC 7807 ProblemDetail. "
                        + "POST /api/v1/readings exists only in the dev profile (IoT simulator).")
                .version("0.1.0"));
    }
}
