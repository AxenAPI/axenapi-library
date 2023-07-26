package org.axenix.axenapi.annotation;

public @interface KafkaHandlerHeader {
    String header() default "";
    boolean required() default false;
}
