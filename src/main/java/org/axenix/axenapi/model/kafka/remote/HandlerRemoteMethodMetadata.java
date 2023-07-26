package org.axenix.axenapi.model.kafka.remote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axenix.axenapi.model.ClassData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandlerRemoteMethodMetadata {
    private String methodPropertyName;
    private String variablesPropertyName;
    private ClassData methodPropertyType;
    private List<RemoteMethodMetadata> methods;
}
