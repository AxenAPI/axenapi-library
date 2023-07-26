package org.axenix.axenapi.model.jms;

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
public class JmsHandlerMetadata {
    private String jmsTemplateName;
    private ClassData payload;
    private String destination;
    private String description;
    private List<PropertyMetadata> properties;
}
