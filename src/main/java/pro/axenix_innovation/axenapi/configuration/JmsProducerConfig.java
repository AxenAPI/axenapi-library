package pro.axenix_innovation.axenapi.configuration;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import pro.axenix_innovation.axenapi.jms.JmsTemplateRegistry;
import pro.axenix_innovation.axenapi.service.JmsSenderService;
import pro.axenix_innovation.axenapi.service.impl.DefaultJmsSenderService;

import javax.jms.Message;

@Configuration
@ConditionalOnProperty(prefix = "axenapi.jms.swagger", name = "enabled", havingValue = "true")
@ConditionalOnClass({Message.class, JmsTemplate.class}) // TODO
@RequiredArgsConstructor
public class JmsProducerConfig {
    @Bean
    public JmsTemplateRegistry jmsTemplateRegistry(JmsTemplate jmsTemplate) {
        JmsTemplateRegistry registry = new JmsTemplateRegistry();
        registry.register("default", jmsTemplate);
        return registry;
    }

    @Bean
    public JmsSenderService jmsSenderService(JmsTemplateRegistry jmsTemplateRegistry) {
        return new DefaultJmsSenderService(jmsTemplateRegistry);
    }
}
