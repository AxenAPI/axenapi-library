package org.axenix.axenapi.jms;

import org.springframework.jms.core.JmsTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JmsTemplateRegistry {
    private final Map<String, JmsTemplate> map = new HashMap<>();

    public void register(String name, JmsTemplate template) {
        map.put(name, template);
    }

    public Optional<JmsTemplate> receive(String name) {
        return Optional.ofNullable(map.get(name));
    }
}
