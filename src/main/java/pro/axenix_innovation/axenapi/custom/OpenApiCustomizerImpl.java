package pro.axenix_innovation.axenapi.custom;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import pro.axenix_innovation.axenapi.annotation.Outgoing;
import pro.axenix_innovation.axenapi.consts.Info;

import java.util.HashSet;

/**
 * The customizer is used to parse types annotated with {@link Outgoing @Outgoing}
 * that were not parsed by the OpenAPI implementation
 * (OpenAPI considers only those types that are used in the controller methods).
 */
public class OpenApiCustomizerImpl implements OpenApiCustomiser, EnvironmentAware {
    private Environment environment;
    private final HashSet<Class<Object>> handledClasses = new HashSet<>();

    @Override
    public void customise(OpenAPI openApi) {
        var outgoingPackage = environment.getProperty(Info.PROP_OUTGOING_TYPES_PACKAGE);

        if (StringUtils.isEmpty(outgoingPackage)) {
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
                            cls, null, null);
                }
            } catch (ClassNotFoundException ignored) {}
        });

        // the method is called at the end of the parsing, the set can be cleared
        handledClasses.clear();
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
