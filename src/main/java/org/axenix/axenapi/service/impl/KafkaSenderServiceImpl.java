
/*
 * Copyright (C) 2023 Axenix Innovations LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.axenix.axenapi.service.impl;

import jakarta.servlet.http.HttpServletResponse;
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
        // TODO get headers handler
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
        // TODO get headers handler for message
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
