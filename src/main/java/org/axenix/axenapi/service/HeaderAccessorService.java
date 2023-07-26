package org.axenix.axenapi.service;

import org.axenix.axenapi.utils.KafkaHeaderAccessor;

import java.util.Map;

public interface HeaderAccessorService {
    KafkaHeaderAccessor getHeaderAccessor(String topicName, Map<String, String> params);
}
