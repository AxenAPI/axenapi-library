package org.axenix.axenapi.annotation;

public @interface RemoteMethodVariable {
    /**
     * Description of method variable
     */
    String description();

    /**
     * Name of variable field. Should be appropriate to Java language.
     */
    String propertyFieldName();

    /**
     * Variable type.
     */
    Class<?> type();
}
