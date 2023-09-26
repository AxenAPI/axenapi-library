
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotation to mark the class that handles the object received from the jms.
 */
@Target(ElementType.TYPE)
public @interface JmsHandler {
    /**
     * @return Annotation name for searching handlers {@link org.springframework.jms.core.JmsTemplate}
     * {@link JmsTemplateRegistry}
     */
    String jmsTemplateName();

    /**
     * @return list of parameters are sent in request and put in {@link javax.jms.Message##setObjectProperty(String, Object)}
     */
    JmsProperty[] properties() default {};
    Class<?> payload();
    String destination();
    String description() default "";
}
