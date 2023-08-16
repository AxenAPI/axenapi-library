package org.axenix.axenapi.jms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotation to mark the class that handles the object received from the jms.
 */
@Target(ElementType.TYPE)
public @interface JmsHandler {
    /**
     * @return Annotation name for searching handlers {@link org.springframework.jms.core.JmsTemplate}
     * {@link JmsTemplateRegistry}
     */
    String jmsTemplateName();

    /**
     * @return list of parameters are sent in request and put in {@link javax.jms.Message##setObjectProperty(String, Object)}
     */
    JmsProperty[] properties() default {};
    Class<?> payload();
    String destination();
    String description() default "";
}
