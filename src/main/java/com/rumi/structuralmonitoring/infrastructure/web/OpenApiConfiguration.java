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
                .description("REST API of the Structural Monitoring bounded context: "
                        + "sensor readings and the digital twin. No REST endpoint is implemented yet; "
                        + "the service currently publishes the SensorReadingRecorded event over RabbitMQ.")
                .version("0.1.0"));
    }
}
