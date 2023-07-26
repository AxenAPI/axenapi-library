package org.axenix.axenapi.service.impl;

import org.axenix.axenapi.service.ResponseHeaderExtractorService;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;
import org.axenix.axenapi.utils.KafkaHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseHeaderExtractorServiceImpl implements ResponseHeaderExtractorService {
    private final Map<String, HeaderType> kafkaHeaders = new HashMap<>();

    public void appendHeader(String kafkaHeader, HeaderType headerType) {
        kafkaHeaders.put(kafkaHeader, headerType);
    }

    @Override
    public Map<String, String> extractHeaders(KafkaHeaderAccessor headerAccessor) {
        return headerAccessor.toMap().entrySet()
                .stream()
                .filter(entry -> kafkaHeaders.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            byte[] value = (byte[])entry.getValue();

                            switch (kafkaHeaders.get(entry.getKey())) {
                                case UUID_TYPE:
                                    return KafkaHelper.bytesToUUID(value).toString();
                                case STRING_TYPE:
                                    return new String(value);
                                case INTEGER_TYPE:
                                    return (KafkaHelper.bytesToInt(value)).toString();
                            }

                            return new String(value);
                        }
                ));
    }
}
