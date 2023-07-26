package org.axenix.axenapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassData {
    private String qualifiedClassName;
    private String packageName;
    private String simpleClassName;
    @Builder.Default
    private boolean isArray = false;
}
