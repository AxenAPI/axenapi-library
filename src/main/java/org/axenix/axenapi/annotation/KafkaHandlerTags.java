package org.axenix.axenapi.annotation;

public @interface KafkaHandlerTags {
    /**
     * list of tags
     */
    String[] tags() default {};
}
