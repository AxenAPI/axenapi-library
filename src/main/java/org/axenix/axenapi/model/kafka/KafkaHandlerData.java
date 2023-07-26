package org.axenix.axenapi.model.kafka;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.axenix.axenapi.model.ParamsData;
import org.axenix.axenapi.model.ReturnedData;
import org.axenix.axenapi.model.VariableData;
import org.axenix.axenapi.model.kafka.remote.HandlerRemoteMethodMetadata;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaHandlerData {
    private String description;
    private VariableData variableData;
    private ReturnedData returnedData;
    private boolean secured;
    private String securityScheme;
    private List<ParamsData> params;
    private HandlerRemoteMethodMetadata handlerRemoteMethod;
    private List<String> tags;
}
