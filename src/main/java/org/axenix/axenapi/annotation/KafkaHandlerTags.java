package org.axenix.axenapi.annotation;

public @interface KafkaHandlerTags {
    /**
     * Список тэгов хендлера
     */
    String[] tags() default {};
}
