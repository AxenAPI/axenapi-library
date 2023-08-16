package org.axenix.axenapi.annotation;

public @interface RemoteMethod {
    /**
     * name of method
     */
    String propertyValue();

    /**
     * description of method
     */
    String description();

    /**
     * list of methods arguments
     */
    RemoteMethodVariable[] variables() default {};

    /**
     * List of arguments pf method as one DTO.
     *
     * Overridden data passed in variables
     */
    Class<?> variablesType() default Void.class;

    /**
     * list of methods tags
     */
    String[] tags() default {};
}
