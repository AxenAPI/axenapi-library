package org.axenix.axenapi.service;

import java.util.Map;
import java.util.UUID;

public interface TokenProducerService {
    void readTokenFromParams(Map<String, Object> params, UUID messageId);
    Map<String, Object> replaceTokenIntoParams(Map<String, Object> params, UUID messageId);
}
