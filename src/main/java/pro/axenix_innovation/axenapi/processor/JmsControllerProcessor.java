
/*
 * Copyright (C) 2023 Axenix Innovations LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package pro.axenix_innovation.axenapi.processor;

import pro.axenix_innovation.axenapi.code.generator.JmsControllerCodeGenerator;
import pro.axenix_innovation.axenapi.jms.JmsHandler;
import pro.axenix_innovation.axenapi.jms.JmsProperty;
import pro.axenix_innovation.axenapi.model.ClassData;
import pro.axenix_innovation.axenapi.model.jms.JmsHandlerMetadata;
import pro.axenix_innovation.axenapi.model.jms.PropertyMetadata;
import pro.axenix_innovation.axenapi.utils.ElementHelper;
import pro.axenix_innovation.axenapi.utils.AxenAPIProperties;

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

@SupportedAnnotationTypes(value = {"pro.axenix_innovation.axenapi.jms.JmsHandler"})
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

            /* Find all places with annotation. */
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(jmsHandlerAnnotationElement);

            /* Put result into the list. */
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

                /* get the typr. */
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
