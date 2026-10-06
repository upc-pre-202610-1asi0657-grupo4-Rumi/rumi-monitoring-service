package com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq;

import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingRecordedPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorReadingRecordedPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final boolean messagingEnabled;

    public SensorReadingRecordedPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rumi.messaging.enabled:true}") boolean messagingEnabled
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingEnabled = messagingEnabled;
    }

    public void publish(SensorReadingRecorded event) {
        if (!messagingEnabled) {
            LOGGER.info("Messaging disabled, sensor reading event {} not published", event.eventId());
            return;
        }
        rabbitTemplate.convertAndSend(
                StructuralMonitoringMessagingConfiguration.SENSOR_READING_EXCHANGE,
                StructuralMonitoringMessagingConfiguration.SENSOR_READING_ROUTING_KEY,
                event
        );
    }
}
