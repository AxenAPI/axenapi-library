package org.axenix.axenapi.jms;

import javax.jms.Message;

/**
 * Обработчик jms сообщений перед отправкой.
 * В рамках библиотеки является крайней точкой обработки сообщения
 */
public interface JmsMessagePostProcessor {
    /**
     * Метод, внутри которого происходят изменения объекта сообщения
     * @param message изменяемый объект сообщения
     */
    void process(Message message);
}
