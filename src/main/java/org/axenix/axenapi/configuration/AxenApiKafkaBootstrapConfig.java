
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

import org.axenix.axenapi.service.KafkaBootstrapForAxenAPI;
import org.axenix.axenapi.service.impl.KafkaBootstrapForAxenAPIBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
public class AxenApiKafkaBootstrapConfig {
    @Bean
    @ConditionalOnMissingBean
    public KafkaBootstrapForAxenAPI kafkaBootstrapForAxenAPIBean() {
        return new KafkaBootstrapForAxenAPIBean();
    }
}
