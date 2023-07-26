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
public class RemoteMethodMetadata {
    private String propertyValue;
    private String description;
    private List<RemoteMethodVariableMetadata> variables;
    private ClassData variablesType;
    private List<String> tags;
}
