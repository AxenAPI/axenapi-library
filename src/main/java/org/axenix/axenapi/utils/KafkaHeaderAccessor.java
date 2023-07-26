package org.axenix.axenapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.axenix.axenapi.consts.Headers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Slf4j
public class KafkaHeaderAccessor {
    public static KafkaHeaderAccessor create() {
        log.debug("KafkaHeaderAccessor.create()");
        return new KafkaHeaderAccessor();
    }

    public static KafkaHeaderAccessor ofMap(Map<String, Object> headersMap) {
        log.debug("KafkaHeaderAccessor.ofMap(headersMap) headersMap = " + headersMap.toString());
        return new KafkaHeaderAccessor();
    }

    public static UUID fromUUIDHeader(Map<String, Object> headers, Headers messageId) {
        log.debug("KafkaHeaderAccessor.fromUUIDHeader(headers, messageId) headers = " + headers.toString(), "messageId = " + messageId);
        return null;
    }

    public static String fromStringHeader(Map<String, Object> headers, Headers serviceAccessToken) {
        log.debug("KafkaHeaderAccessor.fromUUIDHeader(headers, serviceAccessToken) headers = " + headers.toString(), "serviceAccessToken = " + serviceAccessToken);
        return null;
    }

    public UUID messageId() {
        log.debug("KafkaHeaderAccessor.messageId()");
        return null;
    }

    public UUID messageId(UUID randomUUID) {
        log.debug("KafkaHeaderAccessor.messageId(UUID) UUID = " + randomUUID);
        return null;
    }

    public UUID messageGroupId() {
        log.debug("KafkaHeaderAccessor.messageGroupId()");
        return null;
    }

    public UUID messageGroupId(UUID randomUUID) {
        log.debug("KafkaHeaderAccessor.messageGroupId(UUID) UUID = " + randomUUID);
        return null;
    }

    public UUID correlationId() {
        log.debug("KafkaHeaderAccessor.correlationId()");
        return null;
    }


    public UUID correlationId(UUID randomUUID) {
        log.debug("KafkaHeaderAccessor.correlationId(UUID) UUID = " + randomUUID);
        return null;
    }

    public UUID sourceInstanceId() {
        log.debug("KafkaHeaderAccessor.sourceInstanceId()");
        return null;
    }

    public String sourceInstanceId(String swagger4kafka) {
        log.debug("KafkaHeaderAccessor.sourceInstanceId(swagger4kafka) UUID = " + swagger4kafka);
        return null;
    }

    public UUID traceId() {
        log.debug("KafkaHeaderAccessor.traceId()");
        return null;
    }

    public UUID traceId(UUID randomUUID) {
        log.debug("KafkaHeaderAccessor.traceId(UUID) UUID = " + randomUUID);
        return null;
    }

    public void topic(String topicName) {
        log.debug("KafkaHeaderAccessor.topic(topicName) topicName = " + topicName);
    }

    public Map<String, Object> toMap() {
        log.debug("KafkaHeaderAccessor.toMap()");
        return new HashMap<>();
    }
}
