package com.rumi.structuralmonitoring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class DevProfileContextTest {

    @Autowired
    private DataSource dataSource;

    @Value("${rumi.messaging.enabled}")
    private boolean messagingEnabled;

    @Test
    void runsOnAnInMemoryDatabaseWithMessagingDisabled() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:");
        }
        assertThat(messagingEnabled).isFalse();
    }
}
