
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
