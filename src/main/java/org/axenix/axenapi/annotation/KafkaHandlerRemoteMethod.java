package org.axenix.axenapi.annotation;

public @interface KafkaHandlerRemoteMethod {
    /**
     * Property for method value substitution
     *
     * Should not be empty, should contain the path to the property.
     * Nested properties describes with '.'
     * Example: task.method
     */
    String methodPropertyName();

    /**
     * Property for arguments of method.
     *
     * Should not be empty, should contain the path to the property.
     * Nested properties describes with '.'
     * Example: task.variables
     */
    String variablesPropertyName();

    /**
     * Type of property with method name.
     */
    Class<?> methodPropertyType() default String.class;

    /**
     * Descriptions of methods: name and arguments
     */
    RemoteMethod[] methods();
}
