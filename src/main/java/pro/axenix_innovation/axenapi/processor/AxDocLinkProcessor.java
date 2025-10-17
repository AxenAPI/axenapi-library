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

import lombok.extern.slf4j.Slf4j;
import pro.axenix_innovation.axenapi.annotation.AxDocLink;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Annotation processor for @AxDocLink annotation.
 * Validates that the annotation is used correctly on methods.
 * The actual processing of documentation links is handled by AxDocLinkConfiguration at runtime.
 */
@SupportedAnnotationTypes(value = {"pro.axenix_innovation.axenapi.annotation.AxDocLink"})
@Slf4j
public class AxDocLinkProcessor extends AbstractProcessor {
    
    private Messager messager;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    @SuppressWarnings({"java:S106", "java:S3457"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            TypeElement axDocLinkAnnotationElement = annotations.stream().findFirst().orElse(null);

            if (axDocLinkAnnotationElement == null) {
                return false;
            }

            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(axDocLinkAnnotationElement);

            if (annotatedElements.isEmpty()) {
                return false;
            }

            // Validate all methods with @AxDocLink annotation
            for (Element annotatedElement : annotatedElements) {
                if (!Objects.equals(annotatedElement.getKind(), ElementKind.METHOD)) {
                    messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Annotation @AxDocLink can only be applied to methods",
                            annotatedElement
                    );
                    continue;
                }

                ExecutableElement method = (ExecutableElement) annotatedElement;
                AxDocLink annotation = method.getAnnotation(AxDocLink.class);
                
                if (annotation == null || annotation.value() == null || annotation.value().isEmpty()) {
                    messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Annotation @AxDocLink must have a non-empty value (relative path to documentation file)",
                            annotatedElement
                    );
                    continue;
                }

                // Get the fully qualified method name for logging
                TypeElement classElement = (TypeElement) method.getEnclosingElement();
                String className = classElement.getQualifiedName().toString();
                String methodName = method.getSimpleName().toString();
                
                System.out.printf("Validated @AxDocLink on method %s.%s with path: %s\n", 
                        className, methodName, annotation.value());
            }

            return false;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }
}
