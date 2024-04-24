
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

package pro.axenix_innovation.axenapi.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import pro.axenix_innovation.axenapi.service.KafkaClient4AxenAPI;
import pro.axenix_innovation.axenapi.service.impl.KafkaClient4AxenAPIImpl;

@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
@ConditionalOnClass(KafkaTemplate.class)    // TODO
@AutoConfigureAfter(KafkaProducerConfig.class)
public class KafkaClientConfig {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaClientConfig(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaClient4AxenAPI kafkaClient4AxenAPI() {
        return new KafkaClient4AxenAPIImpl(kafkaTemplate);
    }
}
