
/*
 * Copyright (C) 2023 Axenix Innovations LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.axenix.axenapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axenix.axenapi.jms.JmsMessagePostProcessor;
import org.axenix.axenapi.jms.JmsTemplateRegistry;
import org.axenix.axenapi.service.JmsSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import javax.jms.JMSException;
import org.springframework.jms.core.JmsTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultJmsSenderService implements JmsSenderService {
    private final JmsTemplateRegistry registry;

    @Autowired(required = false)
    protected List<JmsMessagePostProcessor> postProcessors = new ArrayList<>();

    @Override
    public void send(String jmsTemplateName, Object payload, String destination, Map<String, String> params) {
        log.info("send to connection {}", jmsTemplateName);
        log.info("destination: {}", destination);
        log.info("payload: {}", payload);
        log.info("params:");
        params.forEach((key, value) -> log.debug("key: {} value: {}", key, value));

        JmsTemplate template = registry.receive(jmsTemplateName)
            .orElseThrow(() -> new IllegalStateException("JmsTemplate " + jmsTemplateName + " not find"));

        template.convertAndSend(destination, payload, m -> {
            params.forEach((k, v) -> {
                try {
                    m.setObjectProperty(k, v);
                } catch (JMSException jmsException) {
                    log.error("Can not put parameter for message.", jmsException);
                }
            });

            postProcessors.forEach(p -> p.process(m));

            return m;
        });
    }
}
