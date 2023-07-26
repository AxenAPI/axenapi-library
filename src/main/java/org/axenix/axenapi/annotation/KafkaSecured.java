package org.axenix.axenapi.annotation;

public @interface KafkaSecured {
    /**
     * Set name of openapi security scheme. Default value is "Internal-Token".
     * You can use different schemes for different listeners.
     * You should describe OpenAPI security scheme before using it in this annotation.
     */
    String name() default  "Internal-Token";
}

