package org.axenix.axenapi.service;

public interface RemoteMethodService {

    void replaceMethodAndVariables(
            Object originalDto,
            String methodPropertyName,
            Object methodPropertyValue,
            String valuesPropertyName,
            Object variableData
    ) throws Exception;
}
