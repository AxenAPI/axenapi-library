package org.axenix.axenapi.processor;

import org.axenix.axenapi.code.generator.JmsControllerCodeGenerator;
import org.axenix.axenapi.jms.JmsHandler;
import org.axenix.axenapi.jms.JmsProperty;
import org.axenix.axenapi.model.ClassData;
import org.axenix.axenapi.model.jms.JmsHandlerMetadata;
import org.axenix.axenapi.model.jms.PropertyMetadata;
import org.axenix.axenapi.utils.ElementHelper;
import org.axenix.axenapi.utils.AxenAPIProperties;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(value = {"org.axenix.swagger4kafka.annotation.jms.JmsHandler"})
public class JmsControllerProcessor extends AbstractProcessor {
    private Messager messager;
    private ElementHelper helper;
    private JmsControllerCodeGenerator codeGenerator;
    private AxenAPIProperties properties;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        messager = processingEnvironment.getMessager();
        helper = new ElementHelper(processingEnvironment);
        codeGenerator = new JmsControllerCodeGenerator(processingEnvironment.getFiler());
        properties = new AxenAPIProperties(processingEnvironment.getFiler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            TypeElement jmsHandlerAnnotationElement = annotations.stream().findFirst().orElse(null);

            if (jmsHandlerAnnotationElement == null) {
                return false;
            }

            /* Пройдем по всем элементам, использующим данную аннотацию. */
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(jmsHandlerAnnotationElement);

            /* Собираем все результаты анализа в список. */
            List<JmsHandlerMetadata> handlers = new ArrayList<>();

            for (Element annotatedElement : annotatedElements) {
                if (!Objects.equals(annotatedElement.getKind(), ElementKind.CLASS)) {
                    messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation JmsHandler does support only classes",
                            annotatedElement
                    );
                    continue;
                }

                String packageName = properties.getPackageName();
                if (packageName != null && !packageName.isBlank()
                        && !annotatedElement.asType().toString().startsWith(packageName)) {
                    continue;
                }

                JmsHandler annotation = annotatedElement.getAnnotation(JmsHandler.class);

                /* Получаем тип. */
                TypeMirror payloadTypeMirror = helper.getAnnotationValue(
                        helper.getAnnotationMirrorByAnnotation(annotatedElement, JmsHandler.class),
                        "payload"
                );

                handlers.add(JmsHandlerMetadata
                    .builder()
                    .payload(classDataByTypeMirror(payloadTypeMirror))
                    .destination(annotation.destination())
                    .description(annotation.description())
                    .jmsTemplateName(annotation.jmsTemplateName())
                    .properties(propertiesMetadataByAnnotations(annotation.properties()))
                    .build()
                );
            }

            codeGenerator.writeFile(handlers);

            return false;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return true;
    }

    private ClassData classDataByTypeMirror(TypeMirror typeMirror) {
        return ClassData.builder()
                .simpleClassName(helper.getClassNameByTypeMirror(typeMirror))
                .packageName(helper.getPackageNameByTypeMirror(typeMirror))
                .qualifiedClassName(helper.getQualifiedClassNameByTypeMirror(typeMirror))
                .build();
    }

    private List<PropertyMetadata> propertiesMetadataByAnnotations(JmsProperty[] properties) {
        return Arrays.stream(properties)
                .map(property -> new PropertyMetadata(property.name(), property.required()))
                .collect(Collectors.toList());
    }
}
