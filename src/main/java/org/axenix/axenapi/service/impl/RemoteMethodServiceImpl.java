
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


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.axenix.axenapi.service.RemoteMethodService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class RemoteMethodServiceImpl implements RemoteMethodService {
    private final ObjectMapper objectMapper;

    @Override
    public void replaceMethodAndVariables(
            Object originalDto,
            String methodPropertyName,
            Object methodPropertyValue,
            String valuesPropertyName,
            Object variableData
    ) throws Exception {
        setField(originalDto, methodPropertyName, methodPropertyValue);

        Map<String, Object> variables = objectMapper.readValue(
                objectMapper.writeValueAsString(variableData),
                new TypeReference<HashMap<String,Object>>() {}
        );

        setField(originalDto, valuesPropertyName, variables);
    }

    private void setField(Object object, String absoluteFieldPath, Object value) throws Exception {
        List<String> fieldPathList = Arrays.stream(absoluteFieldPath.split("\\."))
                .collect(Collectors.toList());

        String fieldPath  = fieldPathList.get(0);

        Class<?> clazz = object.getClass();

        Field field = clazz.getDeclaredField(fieldPath);
        field.setAccessible(true);

        if (fieldPathList.size() == 1) {

            field.set(object, value);
            return;
        }

        fieldPathList.remove(0);
        String nextAbsoluteFieldPath = String.join(".", fieldPathList);

        setField(field.get(object), nextAbsoluteFieldPath, value);
    }
}
