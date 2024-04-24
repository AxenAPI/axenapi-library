package pro.axenix_innovation.axenapi.configuration;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.axenix_innovation.axenapi.service.RabbitSenderService;
import pro.axenix_innovation.axenapi.service.impl.RabbitSenderServiceImpl;

@Configuration
@ConditionalOnProperty(prefix = "axenapi.kafka.swagger", name = "enabled", havingValue = "true")
@ConditionalOnClass({RabbitTemplate.class, Channel.class})  // TODO
@RequiredArgsConstructor
public class RabbitProducerConfig implements InitializingBean {
    private final RabbitTemplate rabbitTemplate;

    @Bean
    RabbitSenderService rabbitSenderService(RabbitTemplate rabbitTemplate) {
        return new RabbitSenderServiceImpl(rabbitTemplate);
    }

    @Override
    public void afterPropertiesSet() {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }
}
