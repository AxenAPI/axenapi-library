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
