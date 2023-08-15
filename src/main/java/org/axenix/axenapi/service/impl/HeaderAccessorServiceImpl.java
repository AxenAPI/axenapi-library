package org.axenix.axenapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.axenix.axenapi.consts.Headers;
import org.axenix.axenapi.service.HeaderAccessorService;
import org.axenix.axenapi.service.TokenProducerService;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;
import org.axenix.axenapi.utils.KafkaHelper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class HeaderAccessorServiceImpl implements HeaderAccessorService {
    private final Map<String, String> mainHeaderNames = new HashMap<>();
    private final Map<String, String> kafkaHeaderNames;
    private final Map<String, String> uuidHeaderNames = new HashMap<>();
    private final Map<String, String> intHeaderNames = new HashMap<>();
    private final TokenProducerService tokenProducerService;

    public HeaderAccessorServiceImpl(TokenProducerService tokenProducerService) {
        // TODO filter double values in KafkaHeaders
        List<Field> fields = Arrays.stream(KafkaHeaders.class.getDeclaredFields()).collect(Collectors.toList());
        List<String> names = fields.stream().map(f -> getFieldStringValue(KafkaHeaders.class, f).map(Object::toString).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> namesDistinct = names.stream().distinct().collect(Collectors.toList());
        kafkaHeaderNames = namesDistinct.stream()
            .collect(
                Collectors.toMap(s -> s, Function.identity())
            );
        Set<String> uuidHeaderNameSet = new HashSet<>(Arrays.asList(
            Headers.MESSAGE_GROUP_ID.name(),
            Headers.MESSAGE_ID.name(),
            Headers.CORRELATION_ID.name(),
            Headers.CHAIN_ID.name(),
            Headers.ID_CHUNKS_MESSAGE.name()
        ));
        Set<String> intHeaderNameSet = new HashSet<>(Arrays.asList(
            Headers.PROCESSING_RESULT.name(),
            Headers.CHUNK_NUMBER.name(),
            Headers.CHUNK_COUNT.name()
        ));

        Arrays.stream(Headers.values())
            .filter(e ->
                !uuidHeaderNameSet.contains(e.name())
                && !intHeaderNameSet.contains(e.name())
            )
            .forEach(key ->
                executeOnManyVariants(key.name(), k -> mainHeaderNames.put(k, k))
            );

        uuidHeaderNameSet.forEach(key ->
            executeOnManyVariants(key, k -> uuidHeaderNames.put(k, k))
        );

        intHeaderNameSet.forEach(key ->
            executeOnManyVariants(key, k -> intHeaderNames.put(k, k))
        );

        this.tokenProducerService = tokenProducerService;
    }

    private void executeOnManyVariants(String str, Consumer<String> consumer) {
        Arrays.asList(str, toCamelCase(str, "_"), toCamelCase(str, "-")).forEach(consumer);
    }

    @SuppressWarnings("java:S3011")
    private static Optional<Object> getFieldStringValue(Object target, Field field) {
        try {
            field.setAccessible(true);
            return Optional.ofNullable(field.get(target));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return Optional.empty();
    }

    private static String toCamelCase(String str, String delimiter) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        str = str.toLowerCase();
        String[] split = str.split(delimiter);

        if (split.length < 2) {
            return str;
        }

        StringBuilder result = new StringBuilder(split[0]);

        for (int i = 1; i < split.length; i++) {
            String s = split[i];

            if (s.isEmpty()) {
                continue;
            }

            StringBuilder b = new StringBuilder(s);
            b.replace(0, 1, b.substring(0, 1).toUpperCase());

            result.append(b);
        }

        return result.toString();
    }

    public KafkaHeaderAccessor getHeaderAccessor(String topicName, Map<String, String> params) {
        KafkaHeaderAccessor headerAccessor = paramsConvertToAccessors(params);

        if (headerAccessor.messageId() == null) {
            headerAccessor.messageId(UUID.randomUUID());
        }
        if (headerAccessor.messageGroupId() == null) {
            headerAccessor.messageGroupId(UUID.randomUUID());
        }
        if (headerAccessor.correlationId() == null) {
            headerAccessor.correlationId(UUID.randomUUID());
        }
        if (headerAccessor.sourceInstanceId() == null) {
            headerAccessor.sourceInstanceId("axenapi");
        }

        if (headerAccessor.traceId() == null) {
            headerAccessor.traceId(UUID.randomUUID());
        }

        headerAccessor.topic(topicName);

        tokenProducerService.readTokenFromParams(headerAccessor.toMap(), headerAccessor.messageId());

        return headerAccessor;
    }

    private KafkaHeaderAccessor paramsConvertToAccessors(Map<String, String> params) {
        Map<String, Object> headersMap = new HashMap<>();

        params.forEach((key, value) -> {
            if (uuidHeaderNames.containsKey(key)) {
                headersMap.put(uuidHeaderNames.get(key), KafkaHelper.uuidToBytes(UUID.fromString(value)));
            } else if (intHeaderNames.containsKey(key)) {
                headersMap.put(uuidHeaderNames.get(key), KafkaHelper.intToBytes(Integer.parseInt(value)));
            } else {
                String result = mainHeaderNames.get(key);

                if (result == null) {
                    executeOnManyVariants(key, k -> {
                        if (kafkaHeaderNames.containsKey(KafkaHeaders.PREFIX + k)) {
                            headersMap.put(kafkaHeaderNames.get(KafkaHeaders.PREFIX + k), value.getBytes());
                        }
                    });
                } else {
                    headersMap.put(result, value.getBytes());
                }
            }
        });

        return CollectionUtils.isEmpty(headersMap)
            ? KafkaHeaderAccessor.create()
            : KafkaHeaderAccessor.ofMap(headersMap);
    }
}
