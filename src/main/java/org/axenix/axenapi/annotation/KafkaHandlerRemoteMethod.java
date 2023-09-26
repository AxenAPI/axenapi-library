
/*
 * Copyright [yyyy] [name of copyright owner]
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

package org.axenix.axenapi.annotation;

public @interface KafkaHandlerRemoteMethod {
    /**
     * Property for method value substitution
     *
     * Should not be empty, should contain the path to the property.
     * Nested properties describes with '.'
     * Example: task.method
     */
    String methodPropertyName();

    /**
     * Property for arguments of method.
     *
     * Should not be empty, should contain the path to the property.
     * Nested properties describes with '.'
     * Example: task.variables
     */
    String variablesPropertyName();

    /**
     * Type of property with method name.
     */
    Class<?> methodPropertyType() default String.class;

    /**
     * Descriptions of methods: name and arguments
     */
    RemoteMethod[] methods();
}
