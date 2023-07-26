package org.axenix.axenapi.annotation;

public @interface RemoteMethod {
    /**
     * Наименование метода
     */
    String propertyValue();

    /**
     * Описание метода
     */
    String description();

    /**
     * Список переменных метода
     */
    RemoteMethodVariable[] variables() default {};

    /**
     * Список переменных метода в виде одного dto.
     *
     * Переопределеные данные, передаваемые в variables
     */
    Class<?> variablesType() default Void.class;

    /**
     * Список тэгов метода
     */
    String[] tags() default {};
}
