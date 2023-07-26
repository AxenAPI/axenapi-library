package org.axenix.axenapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.axenix.axenapi.service.HeaderAccessorService;
import org.axenix.axenapi.service.KafkaClient4AxenAPI;
import org.axenix.axenapi.service.KafkaSenderService;
import org.axenix.axenapi.service.ResponseHeaderExtractorService;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
public class KafkaSenderServiceImpl implements KafkaSenderService {
    @Value( "${axenapi.headers.sendBytes:false}" )
    private Boolean sendBytes;

    private final HeaderAccessorService headerAccessorService;
    private final KafkaClient4AxenAPI kafkaClient4AxenAPI;
    private final ResponseHeaderExtractorService responseHeaderExtractorService;

    @Override
    public void send(String topicName, Object message, Map<String, String> params, HttpServletResponse servletResponse) {
        // TODO получение обработчика headers у message
        // KafkaHeaderAccessor headerAccessor = headerAccessorService.getHeaderAccessor(topicName, params);
        KafkaHeaderAccessor headerAccessor = new KafkaHeaderAccessor();

        if (params != null && params.size() > 0) {
            var messageBuilder = MessageBuilder.withPayload(message);
            for (var entry : params.entrySet()) {
                if(sendBytes) {
                    messageBuilder.setHeader(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8));
                } else {
                    messageBuilder.setHeader(entry.getKey(), entry.getValue());
                }

            }
            Message msg = messageBuilder.build();
            ProducerRecord producerRecord = new MessagingMessageConverter().fromMessage(msg, topicName);

            kafkaClient4AxenAPI.sendProducerRecord(producerRecord);
        } else {
            kafkaClient4AxenAPI.send(message, topicName, headerAccessor);
        }

        responseHeaderExtractorService.extractHeaders(headerAccessor).entrySet()
                .forEach(entry -> servletResponse.setHeader(entry.getKey(), entry.getValue()));
    }

    @Override
    public void send(String topicName, Object message, Map<String, String> params) {
        // TODO получение обработчика headers у message
        // KafkaHeaderAccessor headerAccessor = headerAccessorService.getHeaderAccessor(topicName, params);
        KafkaHeaderAccessor headerAccessor = new KafkaHeaderAccessor();

        if (params != null && params.size() > 0) {
            var messageBuilder = MessageBuilder.withPayload(message);
            for (var entry : params.entrySet()) {
                if(sendBytes) {
                    messageBuilder.setHeader(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8));
                } else {
                    messageBuilder.setHeader(entry.getKey(), entry.getValue());
                }

            }
            Message msg = messageBuilder.build();
            ProducerRecord producerRecord = new MessagingMessageConverter().fromMessage(msg, topicName);

            kafkaClient4AxenAPI.sendProducerRecord(producerRecord);
        } else {
            kafkaClient4AxenAPI.send(message, topicName, headerAccessor);
        }
    }
}
