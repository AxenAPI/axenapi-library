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

package pro.axenix_innovation.axenapi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for storing documentation links from @AxDocLink annotations.
 * This class is populated by the AxDocLinkProcessor during compilation.
 */
public class AxDocLinkRegistry {
    
    private static final Map<String, String> DOCUMENTATION_LINKS = new ConcurrentHashMap<>();
    
    /**
     * Register a documentation link for a method
     * @param className Fully qualified class name
     * @param methodName Method name
     * @param docLink Documentation file path
     */
    public static void register(String className, String methodName, String docLink) {
        String key = className + "." + methodName;
        DOCUMENTATION_LINKS.put(key, docLink);
    }
    
    /**
     * Get documentation link for a method
     * @param className Fully qualified class name
     * @param methodName Method name
     * @return Documentation file path or null if not found
     */
    public static String getDocumentationLink(String className, String methodName) {
        String key = className + "." + methodName;
        return DOCUMENTATION_LINKS.get(key);
    }
    
    /**
     * Get all registered documentation links
     * @return Map of method keys to documentation links
     */
    public static Map<String, String> getAllLinks() {
        return new HashMap<>(DOCUMENTATION_LINKS);
    }
    
    /**
     * Clear all registered links (mainly for testing)
     */
    public static void clear() {
        DOCUMENTATION_LINKS.clear();
    }
}
