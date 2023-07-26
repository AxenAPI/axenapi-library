package org.axenix.axenapi.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;

public interface KafkaClient4AxenAPI {
    void send(Object message, String topicName, KafkaHeaderAccessor headerAccessor);
    void sendProducerRecord(ProducerRecord<String, Object> record);
}
