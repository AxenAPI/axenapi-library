package org.axenix.axenapi.jms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Аннотацией помечается класс, обрабатывающий дто полученную из jms listener.
 */
@Target(ElementType.TYPE)
public @interface JmsHandler {
    /**
     * @return имя по которому будет произведен поиск {@link org.springframework.jms.core.JmsTemplate}
     * {@link JmsTemplateRegistry}
     */
    String jmsTemplateName();

    /**
     * @return список параметров, который будет передаваться в запросе
     * и заноситься в {@link javax.jms.Message##setObjectProperty(String, Object)}
     */
    JmsProperty[] properties() default {};
    Class<?> payload();
    String destination();
    String description() default "";
}
