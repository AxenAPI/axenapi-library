package org.axenix.axenapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axenix.axenapi.consts.Headers;
import org.axenix.axenapi.jms.JmsTemplateRegistry;
import org.axenix.axenapi.service.*;
import org.axenix.axenapi.service.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "org.axenix.axenapi")
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
