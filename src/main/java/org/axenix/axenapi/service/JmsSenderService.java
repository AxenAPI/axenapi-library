package org.axenix.axenapi.service;

import java.util.Map;

public interface JmsSenderService {
    void send(String jmsTemplateName, Object payload, String destination, Map<String, String> params);
}
