package org.axenix.axenapi.jms;

import javax.jms.Message;

/**
 * JMS message processor (before sending). The last place in the lib for message proceeding before sending.
 */
public interface JmsMessagePostProcessor {
    /**
     * Method where forming final version of message.
     * @param message to send
     */
    void process(Message message);
}
