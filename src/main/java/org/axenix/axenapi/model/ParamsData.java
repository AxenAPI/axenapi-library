package org.axenix.axenapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamsData {
    private String name;
    @Builder.Default
    private Boolean required = Boolean.FALSE;
}
