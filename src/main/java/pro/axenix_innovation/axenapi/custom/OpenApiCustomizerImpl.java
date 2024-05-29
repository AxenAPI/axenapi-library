package pro.axenix_innovation.axenapi.custom;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocAnnotationsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import pro.axenix_innovation.axenapi.annotation.Outgoing;
import pro.axenix_innovation.axenapi.consts.Info;

import java.util.*;

/**
 * The customizer is used to parse types annotated with {@link Outgoing @Outgoing}
 * that were not parsed by the OpenAPI implementation
 * (OpenAPI considers only those types that are used in the controller methods).
 */
public class OpenApiCustomizerImpl implements OpenApiCustomizer, EnvironmentAware {
    private Environment environment;
    private final HashSet<Class<Object>> handledClasses = new HashSet<>();

    @Override
    public void customise(OpenAPI openApi) {
        setIncomingXProp(openApi);

        var outgoingPackage = environment.getProperty(Info.PROP_OUTGOING_TYPES_PACKAGE);

        if (StringUtils.isBlank(outgoingPackage)) {
            // the method is called at the end of the parsing, the set can be cleared
            handledClasses.clear();
            return;
        }

        // find classes annotated with @Outgoing
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Outgoing.class));
        var bd = scanner.findCandidateComponents(outgoingPackage);

        bd.forEach(beanDefinition -> {
            try {
                var cls = Class.forName(beanDefinition.getBeanClassName());
                if (!handledClasses.contains(cls)) {
                    // this will add the type as a component to the OpenAPI spec
                    SpringDocAnnotationsUtils.extractSchema(openApi.getComponents(),
                            cls, null, null, openApi.getSpecVersion());
                }
            } catch (ClassNotFoundException ignored) {}
        });

        // the method is called at the end of the parsing, the set can be cleared
        handledClasses.clear();
    }

    private void setIncomingXProp(OpenAPI openApi) {
        Map<String, List<String>> incoming = new HashMap<>();
        Paths paths = openApi.getPaths();
        for (Map.Entry<String, PathItem> e : paths.entrySet()) {
            String link = e.getKey();
            if(!link.contains("kafka")) continue;
            String[] split = link.split("/");
            String topic = "";
            if(split.length == 4) {
                topic = split[2];
            } else if (split.length == 5) {
                topic = split[3];
            }

            //input schema is last in the method url
            String schemaName = split[split.length - 1];
            if(!incoming.containsKey(schemaName)) {
                ArrayList<String> newList = new ArrayList<>();
                incoming.put(schemaName, newList);
            }
            List<String> topics = incoming.get(schemaName);
            topics.add(topic);
            // System.out.println("schema = " + schemaName + " topics = " + Arrays.toString(topics.toArray()));
        }
        //System.out.println("!!!Add all incoming info!!!");
        for (Map.Entry<String, List<String>> e : incoming.entrySet()) {
            System.out.println("schema = " + e.getKey());
            var schema = openApi.getComponents().getSchemas().get(e.getKey());
            if(schema != null) {
                Map<String, Object> extension = new HashMap<>();
                extension.put("topics", e.getValue());
                schema.addExtension("x-incoming", extension);
            }
            // else System.out.println("Error: no schema " + e.getKey());
            // TODO log else "Error: no schema " + e.getKey()
        }
    }

    public void setHandled(Class<Object> cls) {
        handledClasses.add(cls);
    }

    @Autowired
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
