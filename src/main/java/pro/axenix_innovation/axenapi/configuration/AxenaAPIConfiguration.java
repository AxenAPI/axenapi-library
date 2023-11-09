
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pro.axenix_innovation.axenapi.consts.Headers;
import pro.axenix_innovation.axenapi.jms.JmsTemplateRegistry;
import pro.axenix_innovation.axenapi.service.*;
import pro.axenix_innovation.axenapi.service.impl.*;

@Configuration
@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "pro.axenix_innovation.axenapi")
@AutoConfigureAfter(KafkaProducerConfig.class)
public class AxenaAPIConfiguration {

    @Bean
    public HeaderAccessorService headerAccessorService(TokenProducerService tokenProducerService) {
        return new HeaderAccessorServiceImpl(tokenProducerService);
    }

    @Bean
    public JmsTemplateRegistry jmsTemplateRegistry() {
        return new JmsTemplateRegistry();
    }

    @Bean
    public JmsSenderService jmsSenderService(JmsTemplateRegistry jmsTemplateRegistry) {
        return new DefaultJmsSenderService(jmsTemplateRegistry);
    }

    @Bean
    KafkaSenderService kafkaSenderService(
            HeaderAccessorService headerAccessorService,
            KafkaClient4AxenAPI kafkaClient4AxenAPI,
            ResponseHeaderExtractorService responseHeaderExtractorService
    ) {
        return new KafkaSenderServiceImpl(headerAccessorService, kafkaClient4AxenAPI, responseHeaderExtractorService);
    }

    @Bean
    public ResponseHeaderExtractorService responseHeaderExtractorService() {
        ResponseHeaderExtractorServiceImpl service = new ResponseHeaderExtractorServiceImpl();

        service.appendHeader(Headers.MESSAGE_ID.name(), HeaderType.UUID_TYPE);
        service.appendHeader(Headers.MESSAGE_GROUP_ID.name(), HeaderType.UUID_TYPE);
        service.appendHeader(Headers.TRACE_ID.name(), HeaderType.STRING_TYPE);
        service.appendHeader(Headers.CORRELATION_ID.name(), HeaderType.UUID_TYPE);

        return service;
    }

    @Bean
    public RemoteMethodService modelGeneratorService(ObjectMapper objectMapper) {
        return new RemoteMethodServiceImpl(objectMapper);
    }

    @Bean
    public TokenProducerService tokenProducerService() {
        return new TokenProducerServiceImpl();
    }

//    @ConditionalOnBean(ServiceSecurityConfiguration.class)
//    @Bean
//    public SwaggerKafkaHeaderAspect swaggerKafkaHeaderAspect(
//            ServiceTokenManager serviceTokenManager,
//            TokenProducerService tokenProducerService
//    ) {
//        return new SwaggerKafkaHeaderAspect(serviceTokenManager, tokenProducerService);
//    }
}
