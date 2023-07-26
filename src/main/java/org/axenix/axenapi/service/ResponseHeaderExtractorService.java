package org.axenix.axenapi.service;

import org.axenix.axenapi.utils.KafkaHeaderAccessor;

import java.util.Map;

public interface ResponseHeaderExtractorService {
    Map<String, String> extractHeaders(KafkaHeaderAccessor headerAccessor);
}
