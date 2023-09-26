
/*
 * Copyright [yyyy] [name of copyright owner]
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
