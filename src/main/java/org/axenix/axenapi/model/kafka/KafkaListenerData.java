package org.axenix.axenapi.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaListenerData {
    private String listenerClassName;
    private List<String> topics;
    private List<KafkaHandlerData> handlers;
    private String groupId;
}
