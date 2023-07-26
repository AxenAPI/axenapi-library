package org.axenix.axenapi.annotation;

public @interface RemoteMethodVariable {
    /**
     * Описание переменной метода
     */
    String description();

    /**
     * Наименование поля переменной
     * Должен быть валидным именем переменной Java
     */
    String propertyFieldName();

    /**
     * Тип переменной
     */
    Class<?> type();
}
