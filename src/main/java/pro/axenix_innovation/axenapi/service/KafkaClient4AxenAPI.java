
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

package pro.axenix_innovation.axenapi.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import pro.axenix_innovation.axenapi.utils.KafkaHeaderAccessor;

public interface KafkaClient4AxenAPI {
    void send(Object message, String topicName, KafkaHeaderAccessor headerAccessor);
    void sendProducerRecord(ProducerRecord<String, Object> record);
}
