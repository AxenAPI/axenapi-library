package org.axenix.axenapi.model.kafka.remote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axenix.axenapi.model.ClassData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMethodVariableMetadata {
    private String propertyFieldName;
    private String description;
    private ClassData type;
}
