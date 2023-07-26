package org.axenix.axenapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.axenix.axenapi.service.KafkaClient4AxenAPI;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;
import org.springframework.kafka.core.KafkaTemplate;


@Slf4j
public class KafkaClient4AxenAPIImpl implements KafkaClient4AxenAPI {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaClient4AxenAPIImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Object message, String topicName, KafkaHeaderAccessor headerAccessor) {
        log.debug("KafkaClient4Swagger.send " + "message = " + message + "KafkaHeaderAccessor = " + headerAccessor);
        kafkaTemplate.send(topicName, message);
    }

    public void sendProducerRecord(ProducerRecord<String, Object> record) {
        kafkaTemplate.send(record);
    }
}
