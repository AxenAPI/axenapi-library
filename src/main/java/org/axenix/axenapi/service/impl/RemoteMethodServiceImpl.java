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
