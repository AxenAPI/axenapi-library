package pro.axenix_innovation.axenapi.service;

public interface RabbitSenderService {
    void send(String queue, Object message);
}
