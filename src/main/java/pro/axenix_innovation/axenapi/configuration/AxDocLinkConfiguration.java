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

package pro.axenix_innovation.axenapi.configuration;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import pro.axenix_innovation.axenapi.annotation.AxDocLink;

import java.util.HashMap;

/**
 * Configuration class that adds x-documentation-file-links extension to Swagger/OpenAPI
 * for methods annotated with @AxDocLink.
 */
@Configuration
public class AxDocLinkConfiguration {

    @Bean
    public OperationCustomizer axDocLinkCustomiser() {
        return (operation, handlerMethod) -> {
            // Check if the method has @AxDocLink annotation
            AxDocLink axDocLink = handlerMethod.getMethodAnnotation(AxDocLink.class);
            
            if (axDocLink != null && axDocLink.value() != null && !axDocLink.value().isEmpty()) {
                // Add x-documentation-file-links extension to the operation
                if (operation.getExtensions() == null) {
                    operation.setExtensions(new HashMap<>());
                }
                operation.getExtensions().put("x-documentation-file-links", axDocLink.value());
            }
            
            return operation;
        };
    }
}
