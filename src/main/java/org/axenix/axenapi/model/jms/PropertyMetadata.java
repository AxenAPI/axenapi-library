package org.axenix.axenapi.model.jms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMetadata {
    private String name;
    private Boolean required;
}
