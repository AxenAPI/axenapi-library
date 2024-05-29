package pro.axenix_innovation.axenapi.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.core.annotation.AnnotationUtils;
import pro.axenix_innovation.axenapi.annotation.Outgoing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom implementation that allows extending the OpenAPI json spec
 * with the outgoing message info.
 */
public class ModelResolverCust extends ModelResolver {
    String OUTGOING_EXT_PROP = "x-outgoing";
    String TOPICS_PROP = "topics";
    String TAGS_PROP = "tags";
    String TYPE_PROP = "type";

    OpenApiCustomizerImpl openApiCustomizer;

    public ModelResolverCust(ObjectMapper mapper, OpenApiCustomizerImpl openApiCustomizer) {
        super(mapper);
        this.openApiCustomizer = openApiCustomizer;
    }

    @Override
    public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> next) {
        var model = super.resolve(annotatedType, context, next);

        Class<Object> type = annotatedType.getType() instanceof Class ? (Class<Object>) annotatedType.getType() : null;
        if (type == null)
            return model;

        var annoOutgoing = AnnotationUtils.findAnnotation(type, Outgoing.class);
        if (annoOutgoing == null)
            return model;

        openApiCustomizer.setHandled(type);

        // for now only consider cases where a single schema is referenced
        var schema = context.getDefinedModels().entrySet().stream().findFirst()
                .map(Map.Entry::getValue).orElse(null);
        if (schema == null)
            return model;
        var extensions = schema.getExtensions();
        if (extensions == null) {
            extensions = new HashMap<>();
            schema.setExtensions(extensions);
        }

        Map<String, Object> outgoingMap = null;
        if (!(extensions.get(OUTGOING_EXT_PROP) instanceof Map)) {
            outgoingMap = new HashMap<>();
            extensions.put(OUTGOING_EXT_PROP, outgoingMap);
        }

        outgoingMap.put(TOPICS_PROP, annoOutgoing.topics());
        outgoingMap.put(TAGS_PROP, annoOutgoing.tags());
        outgoingMap.put(TYPE_PROP, annoOutgoing.type().getText());
        return model;
    }
}
