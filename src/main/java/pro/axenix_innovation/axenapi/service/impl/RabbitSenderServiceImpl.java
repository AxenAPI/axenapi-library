package pro.axenix_innovation.axenapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pro.axenix_innovation.axenapi.service.RabbitSenderService;

@RequiredArgsConstructor
public class RabbitSenderServiceImpl implements RabbitSenderService {
    private final RabbitTemplate template;

    @Override
    public void send(String queue, Object message) {
        template.convertAndSend(queue, message);
    }
}
