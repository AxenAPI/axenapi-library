package org.axenix.axenapi.jms;

public @interface JmsProperty {
    String name();
    Class<?> type() default String.class;
    boolean required() default false;
}
