package com.rumi.structuralmonitoring;

import com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq.SensorReadingRecordedPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StructuralMonitoringServiceApplicationTest {

    @Autowired
    private SensorReadingRecordedPublisher publisher;

    @Autowired
    private MessageConverter messageConverter;

    @Test
    void contextLoadsWithoutABroker() {
        assertThat(publisher).isNotNull();
        assertThat(messageConverter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
