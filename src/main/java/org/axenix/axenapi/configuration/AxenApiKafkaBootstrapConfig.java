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
