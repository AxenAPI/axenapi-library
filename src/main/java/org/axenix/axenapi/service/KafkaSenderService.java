package org.axenix.axenapi.service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface KafkaSenderService {
    void send(String topicName, Object message, Map<String, String> params, HttpServletResponse servletResponse);
    void send(String topicName, Object message, Map<String, String> params);
}
