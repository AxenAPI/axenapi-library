package org.axenix.axenapi.annotation;

public @interface KafkaHandlerResponse {
    String replayTopic() default "";
    Class<?> payload();
}
