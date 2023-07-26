package org.axenix.axenapi.service.impl;

import org.axenix.axenapi.service.KafkaBootstrapForAxenAPI;
import org.springframework.beans.factory.annotation.Value;

public class KafkaBootstrapForAxenAPIBean implements KafkaBootstrapForAxenAPI {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    public String getBootstrapAddress() {
        return bootstrapAddress;
    }
}
