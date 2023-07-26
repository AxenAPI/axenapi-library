package org.axenix.axenapi.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axenix.axenapi.model.ParamsData;
import org.axenix.axenapi.model.ReturnedData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointAnnotationsMetadata {
    private String description;
    private List<String> tags;
    private List<ParamsData> params;
    private ReturnedData returnedData;
    private boolean secured;
    private String securityScheme;
}
