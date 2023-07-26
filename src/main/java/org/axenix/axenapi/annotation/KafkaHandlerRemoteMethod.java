package org.axenix.axenapi.annotation;

public @interface KafkaHandlerRemoteMethod {
    /**
     * Свойство для подстановки значений методов
     *
     * Не может быть пустым и содержать пробелы, должно отражать путь до свойства,
     * вложенные свойства описываются через '.'
     * Пример: task.method
     */
    String methodPropertyName();

    /**
     * Свойство для подстановки переменных метода
     *
     * Не может быть пустым и содержать пробелы, должно отражать путь до свойства,
     * вложенные свойства описываются через '.'
     * Пример: task.variables
     */
    String variablesPropertyName();

    /**
     * Описывает тип свойства с методом.
     */
    Class<?> methodPropertyType() default String.class;

    /**
     * Описания методов: название метода и список переменных
     */
    RemoteMethod[] methods();
}
