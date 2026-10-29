package com.rumi.structuralmonitoring.infrastructure.persistence.jpa;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TimescaleHypertableInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleHypertableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.queryForObject(
                "SELECT create_hypertable('sensor_readings', 'timestamp', if_not_exists => TRUE)",
                Object.class
        );
    }
}
