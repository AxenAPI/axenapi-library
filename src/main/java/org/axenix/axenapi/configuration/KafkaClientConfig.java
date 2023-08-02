
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

package org.axenix.axenapi.configuration;

import org.axenix.axenapi.service.KafkaClient4AxenAPI;
import org.axenix.axenapi.service.impl.KafkaClient4AxenAPIImpl;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
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
