package org.axenix.axenapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnedData {
    private ClassData returnedType;
    private String returnedTopicName;
}
